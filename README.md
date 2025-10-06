# TL;DR

> `spring-batch`에서 `ExecutionContext`를 어떻게 활용할 수 있을지 실험해보는 Repository

---

## 간략한 Repo Structure

### Modules
- `our-application`: spring-batch application
- `thirdparty-api`: 배치에서 호출 할 임의의 써드파티 API

### Getting Started
1. `./gradlew :thirdparty-api:bootRun`으로 외부 API를 띄움
2. `./gradlew :our-application:bootRun`으로 인하우스 배치 애플리케이션을 띄움

### thirdparty-api 주문 API

- `POST /orders`
  - 요청 예시
    ```json
    {
      "username": "tester",
      "totalPrice": 10000,
      "appliedYearMonth": "2025-02"
    }
    ```
  - 동일한 신청년월에 이미 주문이 존재하면 `409 CONFLICT`와 함께 `이미 신청된 사용자` 오류를 반환합니다.
  - 성공 시 MySQL `orders` 테이블에 주문이 저장되며, 응답에는 생성된 주문 ID와 생성 시각이 포함됩니다.

## 시나리오 1

### 문제 상황

- 배치 Step에서 외부 API(쓰기 성격)를 호출한다. 이 API는 한 번 호출되면 되돌리기 어렵다.
- 처리 대상 아이템이 100건일 때, 96번째에서 외부 API 장애나 애플리케이션 오류로 실패하면,
  - 잡을 재시작하더라도 이미 처리된 아이템에 대해 동일 API를 다시 호출하게 되어 오류가 반복될 수 있다.
  - 그 결과, 재실행해도 동일 지점에서 재실패가 발생해 회복 탄력성(resilience) 이 확보되지 않는다.

### 가설

- 이 외부 API 호출 상황을 `ExecutionContext`에 저장해 관리하면, 재시도 시 이미 호출된 외부 API를 재호출하는 케이스가 없어지며 멱등성을 지킬 수 있다.

### 증명 과정

- 우리 서비스인 `our-application`과 외부 서드파티인 `thirdparty-api` 모듈을 구성.
- ExecutionContext에 외부 API 호출 여부를 기록하고, 재실행 시 해당 기록을 활용해 중복 호출을 막는다.

### AS-IS

- 기준 커밋 : @5e9fb99b24704f40abc703b2d1ce5b9024dda4a4

현재 구조에서는
`our-application`의 배치 잡이 한 번 실패하면 이후 재시도하더라도 100% 실패하게 되어 있다.

예를 들어, 첫 번째 실행에서 96번째 아이템 처리 중 외부 API 장애나 애플리케이션 오류가 발생하면,
두 번째 재시도 시에는 1번째 아이템부터 다시 처리되며, 이미 첫 실행에서 호출된 외부 API가 다시 호출되어 “이미 처리된 사용자” 오류가 발생한다.
결국 재시도를 반복해도 동일한 오류가 계속 발생해, 잡이 끝내 정상적으로 완료되지 않는다.

물론 이 Exception을 Skip 처리하면 회피는 가능하지만,
여기서는 외부 API의 신뢰성을 낮게 가정하고, 단순 Skip 방식 대신 `ExecutionContext`를 이용한 근본적인 회복력(resilience) 확보를 하고자 한다.

### TO-BE

- `ExecutionContext`를 사용해 **마지막으로 성공한 외부 API 호출의 커서(PK 등)**를 저장한다.
- Job 재시작 시, 이전 성공 지점부터 이어서 외부 API 호출을 수행할 수 있도록 한다.

#### Pros

- `ExecutionContext`를 활용하면 여러 번 재시도해도 복구 가능한(Recoverable) 배치 애플리케이션을 만들 수 있다.
- 커서 기반 접근 덕분에, 이미 처리된 외부 API를 중복 호출하지 않아 멱등성을 확보할 수 있다. 

#### Cons

- 처음, 재시도 했을때의 Reader에서 조회한 데이터가 달라진다면??
  - 마지막으로 처리한 row만 `ExecutionContext`에 저장했기 때문에, 다시 오류가 발생할 수 있음.
  - 이건 애초에 Reader 쪽 조회 쿼리를 잘 작성해야 할 문제 같음.
- `executionContext`를 가져오는 코드가 추가되면서 코드베이스가 다소 지저분 해질 수 있음.

#### 추가 주의사항

현재는 `ExecutionContext`에 PK와 같은 단순한 커서만 저장하고 있다.
하지만 이렇게 되면 다음과 같은 문제가 발생할 수 있다.

- 외부 API를 호출해서, 그 API 결과값을 우리 DB에 따로 저장해두는 경우.
  - 중간 실패 시 이미 성공한 호출(예: 1~95번째)의 DB writer TX가 롤백되어 휘발될 수 있다.
  - 그렇다고 외부 API를 다시 호출할 순 없기 때문에, `our-application`, `thirdparty-api`간 데이터 정합성 문제가 발생한다.

- 그렇다고 `ExecutionContext`에 커서(PK) 뿐만 아닌, API 응답 결과(`List<>`) 까지 캐싱하는건 OOM을 유발할 수도 있을 것 같다.

- 이러한 문제를 해결하기 위해선
  - 외부 호출이 포함된 구간은 chunk(1)으로 설정하여 아이템 단위로 커밋하도록 하는게 좋을 것 같다.

- 만약 데이터가 많거나, 가용성이 필요해서 chunk(1)이 불가능한 상황이라면??
  - 이때는 outbox pattern으로 되려나?? 좀 더 고민해보는것도 좋을듯. 
