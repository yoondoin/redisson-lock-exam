package com.yoondoin.redissonlockexam;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
class RedissonLockExamApplicationTests {

    @Autowired
    private BankService bankService;

    @Test
    void contextLoads() {
    }

    @Test
    @DisplayName("레디스 분산 락 테스트 - 1번 쓰레드만 락 획득 성공")
    void withdrawSameAccountTest() throws InterruptedException {
        //given
        int threadCount = 3;
        Map<String, Boolean> result = new HashMap<>();
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        AtomicInteger i = new AtomicInteger(1);

        List<Thread> workers =
                Stream.generate(() -> new Thread(() -> {
                                result.put(Thread.currentThread().getName(), bankService.withdraw("account-1", 10000L));
                                countDownLatch.countDown();
                        }, "T" + i.getAndIncrement()))
                    .limit(threadCount)
                    .collect(Collectors.toList());

        //when
        workers.forEach(thread -> {
            thread.start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        countDownLatch.await();

        //then
        Assertions.assertThat(result.get("T1")).isEqualTo(true);
        Assertions.assertThat(result.get("T2")).isEqualTo(false);
        Assertions.assertThat(result.get("T3")).isEqualTo(false);
    }

    @Test
    @DisplayName("레디스 분산 락 테스트 - 모든 쓰레드 락 획득 성공")
    void withdrawDifferentAccountTest() throws InterruptedException {
        //given
        int threadCount = 3;
        Map<String, Boolean> result = new HashMap<>();
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        AtomicInteger i = new AtomicInteger(1);
        AtomicInteger j = new AtomicInteger(1);

        List<Thread> workers =
                Stream.generate(() -> new Thread(() -> {
                            result.put(Thread.currentThread().getName(), bankService.withdraw("account-" + i.getAndIncrement(), 10000L));
                            countDownLatch.countDown();
                        }, "T" + j.getAndIncrement()))
                        .limit(threadCount)
                        .collect(Collectors.toList());

        //when
        workers.forEach(thread -> {
            thread.start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        countDownLatch.await();

        //then
        Assertions.assertThat(result.get("T1")).isEqualTo(true);
        Assertions.assertThat(result.get("T2")).isEqualTo(true);
        Assertions.assertThat(result.get("T3")).isEqualTo(true);
    }
}
