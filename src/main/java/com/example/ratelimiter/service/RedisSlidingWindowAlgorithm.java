package com.example.ratelimiter.service;

import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service("RedisSlidingWindow")
public class RedisSlidingWindowAlgorithm implements RateLimitingAlgorithm{

    private final int maxRequestsAllowed;
    private final int slidingWindowSizeInMilliseconds;
    private final RedisTemplate<String, Object> redisTemplate;

    RedisSlidingWindowAlgorithm(RedisTemplate<String, Object> redisTemplate){
        maxRequestsAllowed = 5;
        slidingWindowSizeInMilliseconds = 10000;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isRequestAllowed(String username) {
        long currentTime = System.currentTimeMillis();
            List<Object> logs = redisTemplate.execute(new SessionCallback<>() {
                @Override
                public List execute(RedisOperations operations) {
                    operations.multi();
                    // Add your transactional commands here
                    operations.opsForZSet().removeRangeByScore(username, currentTime-slidingWindowSizeInMilliseconds, Double.MAX_VALUE);
                    operations.opsForZSet().add(username, currentTime, currentTime);
                    operations.expire(username, currentTime+slidingWindowSizeInMilliseconds, TimeUnit.MILLISECONDS);
                    operations.opsForZSet().range(username, 0, -1);
                    return operations.exec();
                }
            });
            if (Objects.nonNull(logs))
                return logs.size() <= maxRequestsAllowed;
            return true;
    }
}
