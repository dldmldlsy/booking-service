package com.booking.service.api.support;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 간단한 분산락 헬퍼. 키 이름만 넘기면 tryLock 후 실행하고 해제한다.
 */
@Component
public class DistributedLockService {

    private final RedissonClient redissonClient;
    private final Duration defaultWait;
    private final Duration defaultLease;

    public DistributedLockService(
            RedissonClient redissonClient,
            @Value("${redisson.lock.wait-seconds:3}") long waitSeconds,
            @Value("${redisson.lock.lease-seconds:5}") long leaseSeconds) {
        this.redissonClient = redissonClient;
        this.defaultWait = Duration.ofSeconds(waitSeconds);
        this.defaultLease = Duration.ofSeconds(leaseSeconds);
    }

    public <T> T executeWithLock(String key, LockCallback<T> callback) {
        return executeWithLock(key, defaultWait, defaultLease, callback);
    }

    public <T> T executeWithLock(String key, Duration wait, Duration lease, LockCallback<T> callback) {
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        try {
            locked = lock.tryLock(wait.toMillis(), lease.toMillis(), TimeUnit.MILLISECONDS);
            if (!locked) {
                throw new IllegalStateException("failed to acquire lock: " + key);
            }
            return callback.doInLock();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("lock interrupted: " + key, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @FunctionalInterface
    public interface LockCallback<T> {
        T doInLock();
    }
}
