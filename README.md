# TL;DR

> `spring-batch`에서 `ExecutionContext`를 어떻게 활용할 수 있을지 실험해보는 Repository

---

# 시나리오 1

## 문제 상황

- Batch Step에서 외부 API를 호출함.
- 이 외부 API는 조회가 아닌 쓰기 API라서, 한 번 호출되면 되돌리기 어려움.

## 가설

- 이 외부 API를 호출 한 상황을 `ExecutionContext`에 저장해 관리하면, 나중에 재시도시에 이미 호출된 외부 API를 재호출하는 케이스가 없어지며 멱등성있는 애플리케이션을 만들 수 있을 것이다.

## 증명 과정

- 우리 서비스인 `our-application`과 외부 서드파티인 `thirdparty-api` 모듈을 생성 
