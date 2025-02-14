package com.github.liaomengge.base_common.helper.lock.distributed.redis;

import com.github.liaomengge.base_common.helper.lock.DistributedLocker;
import com.github.liaomengge.base_common.helper.lock.distributed.callback.AcquiredLockCallback;
import org.redisson.api.RLock;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.github.liaomengge.base_common.helper.lock.distributed.consts.DistributedConst.*;

/**
 * Created by liaomengge on 17/12/19.
 */
public class RedissonLocker implements DistributedLocker {

    private final RedissonConfigManager redissonConfigManager;

    public RedissonLocker(RedissonConfigManager redissonConfigManager) {
        this.redissonConfigManager = redissonConfigManager;
    }

    /**
     * 获取锁, 没过期时间
     *
     * @param lockName
     */
    public void lock(String lockName) {
        RLock rLock = redissonConfigManager.getRedissonClient().getLock(REDIS_LOCKER_PREFIX + lockName);
        rLock.lock();
    }

    /**
     * 获取锁
     *
     * @param lockName
     * @param leaseTime
     * @param timeUnit
     */
    public void lock(String lockName, long leaseTime, TimeUnit timeUnit) {
        RLock rLock = redissonConfigManager.getRedissonClient().getLock(REDIS_LOCKER_PREFIX + lockName);
        rLock.lock(leaseTime, timeUnit);
    }

    /**
     * 尝试获取锁
     *
     * @param lockName
     * @param waitTime
     * @return
     */
    public boolean tryLock(String lockName, long waitTime) {
        return tryLock(lockName, waitTime, TimeUnit.SECONDS);
    }

    /**
     * 尝试获取锁
     *
     * @param lockName
     * @param waitTime
     * @param timeUnit
     * @return
     */
    public boolean tryLock(String lockName, long waitTime, TimeUnit timeUnit) {
        return tryLock(lockName, waitTime, -1, timeUnit);
    }

    /**
     * 尝试获取锁
     *
     * @param lockName
     * @param waitTime
     * @param leaseTime
     * @param timeUnit
     * @return
     */
    public boolean tryLock(String lockName, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock rLock = redissonConfigManager.getRedissonClient().getLock(REDIS_LOCKER_PREFIX + lockName);
        try {
            return rLock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 释放锁
     *
     * @param lockName
     */
    public void unlock(String lockName) {
        RLock rLock = redissonConfigManager.getRedissonClient().getLock(REDIS_LOCKER_PREFIX + lockName);
        if (Objects.nonNull(rLock) && rLock.isHeldByCurrentThread()) {
            rLock.unlock();
        }
    }

    /**
     * 使用分布式锁,默认超时时间
     * 锁不可用时,将一直等待
     *
     * @param lockName
     * @param callback
     * @param <T>
     * @return
     */
    public <T> T lock(String lockName, AcquiredLockCallback<T> callback) {
        return lock(lockName, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, callback);
    }

    /**
     * 使用分布式锁,指定超时时间
     * 锁不可用时,将一直等待
     *
     * @param lockName
     * @param leaseTime 锁超时时间, 超时后,自动释放
     * @param timeUnit
     * @param callback
     * @param <T>
     * @return
     */
    public <T> T lock(String lockName, long leaseTime, TimeUnit timeUnit, AcquiredLockCallback<T> callback) {
        RLock rLock = null;
        try {
            rLock = redissonConfigManager.getRedissonClient().getLock(REDIS_LOCKER_PREFIX + lockName);
            rLock.lock(leaseTime, timeUnit);
            return callback.onSuccess();
        } finally {
            if (Objects.nonNull(rLock) && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    /**
     * 尝试获取锁,获取失败,立刻return
     * 必须手动释放锁
     *
     * @param lockName
     * @param callback
     * @param <T>
     * @return
     */
    public <T> T tryLock(String lockName, AcquiredLockCallback<T> callback) {
        return tryLock(lockName, 0, -1, DEFAULT_TIME_UNIT, callback);
    }

    /**
     * 尝试获取锁,获取失败,立刻return
     * 自动设置释放锁的时间
     *
     * @param lockName
     * @param leaseTime
     * @param timeUnit
     * @param callback
     * @param <T>
     * @return
     */
    public <T> T tryLock(String lockName, long leaseTime, TimeUnit timeUnit, AcquiredLockCallback<T> callback) {
        return tryLock(lockName, 0, leaseTime, timeUnit, callback);
    }

    /**
     * 指定默认超时单位：秒
     *
     * @param lockName
     * @param waitTime
     * @param leaseTime
     * @param callback
     * @param <T>
     * @return
     */
    public <T> T tryLock(String lockName, long waitTime, long leaseTime, AcquiredLockCallback<T> callback) {
        return tryLock(lockName, waitTime, leaseTime, DEFAULT_TIME_UNIT, callback);
    }

    /**
     * 尝试获取锁
     * 指定锁超时时间
     *
     * @param lockName
     * @param waitTime  最多等待时间
     * @param leaseTime 上锁后自动释放锁时间
     * @param timeUnit
     * @param callback
     * @param <T>
     * @return
     */
    public <T> T tryLock(String lockName, long waitTime, long leaseTime, TimeUnit timeUnit,
                         AcquiredLockCallback<T> callback) {
        RLock rLock = redissonConfigManager.getRedissonClient().getLock(REDIS_LOCKER_PREFIX + lockName);
        boolean isSuccess;
        try {
            isSuccess = rLock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (Exception e) {
            return callback.onFailure(e);
        }
        if (isSuccess) {
            try {
                return callback.onSuccess();
            } finally {
                if (Objects.nonNull(rLock)) {
                    if (rLock.isHeldByCurrentThread()) {
                        rLock.unlock();
                        log.info("释放锁[{}]成功", rLock.getName());
                    } else if (rLock.getHoldCount() == 0 && rLock.isLocked()) {
                        log.warn("锁[{}]已expire, 已被自动释放, 请合理设置leaseTime时间", rLock.getName());
                    }
                }
            }
        }
        return callback.onFailure();
    }
}
