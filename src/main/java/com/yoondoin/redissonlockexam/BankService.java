package com.yoondoin.redissonlockexam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankService {
    private final RedissonClient redissonClient;

    public boolean withdraw(String lockName, long amount) {
        boolean isSuccessded = false;

        RLock lock = redissonClient.getLock(lockName);

        try {
            boolean isLocked = lock.tryLock(1, 3, TimeUnit.SECONDS);

            if (isLocked) {
                log.info("### 락 획득 성공");
                /**
                 * business logic
                 */
                Thread.sleep(3000);
                isSuccessded = true;
            }
            else {
                log.info("### 락 획득 실패");
            }
        } catch (InterruptedException e) {
            log.error(String.valueOf(e));
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread())
                lock.unlock();
        }

        return isSuccessded;
    }
}
