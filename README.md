# Redis를 이용한 분산락 예제

### BankService
* RedissonClient의 lock을 이용하며, lock name별로 락 제어
* 락 획득에 성공하면 비지니스 로직 실행

### RedissonLockExamApplicationTests
* withdrawSameAccountTest() : 동일 account(lock name)에 동시 요청시 첫 번째 쓰레드의 요청만 락 획득하는지 테스트
* withdrawDifferentAccountTest() : 다른 account에 동시 요청시 모든 쓰레드가 락 획득하는지 테스트