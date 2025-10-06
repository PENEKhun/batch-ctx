# TL;DR

> `spring-batch`에서 `ExecutionContext`를 어떻게 활용할 수 있을지 실험해보는 Repository

---

## Modules
- `our-application`: spring-batch application
- `thirdparty-api`: 배치에서 호출 할 임의의 써드파티 API

## Getting Started
1. `docker compose up -d`로 실행에 필요한 MySQL을 기동합니다.
2. `./gradlew :thirdparty-api:bootRun`으로 모의 써드파티 API를 띄우고,
3. `./gradlew :our-application:bootRun`으로 배치 애플리케이션을 실행합니다.

## thirdparty-api 주문 API

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

- Batch Step에서 외부 API를 호출함.
- 이 외부 API는 조회가 아닌 쓰기 API라서, 한 번 호출되면 되돌리기 어려움.

### 가설

- 이 외부 API 호출 상황을 `ExecutionContext`에 저장해 관리하면, 재시도 시 이미 호출된 외부 API를 재호출하는 케이스가 없어지며 멱등성을 지킬 수 있다.

### 증명 과정

- 우리 서비스인 `our-application`과 외부 서드파티인 `thirdparty-api` 모듈을 구성.
- ExecutionContext에 외부 API 호출 여부를 기록하고, 재실행 시 해당 기록을 활용해 중복 호출을 막는다.
