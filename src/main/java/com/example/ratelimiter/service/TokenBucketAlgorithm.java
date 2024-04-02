package com.example.ratelimiter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@Service("TokenBucket")
public class TokenBucketAlgorithm implements RateLimitingAlgorithm{

    private final Map<String, Integer> tokenMap;
    private final int bucketSize;

    TokenBucketAlgorithm(@Value("${token.refillRateInMilliseconds}") int refillRateInMilliseconds,
                         @Value("${token.bucketSize}") int bucketSize) {
        tokenMap = new ConcurrentHashMap<>();
        this.bucketSize = bucketSize;
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                tokenMap.keySet().forEach(key->tokenMap.put(key, bucketSize));
            }
        };
        timer.schedule(timerTask, 0, refillRateInMilliseconds);
    }


    @Override
    public boolean isRequestAllowed(String username) {
        if(tokenMap.containsKey(username)) {
            if (tokenMap.get(username) > 0) {
                tokenMap.replace(username, tokenMap.get(username)-1);
                return true;
            }
            else
                return false;
        }
        tokenMap.put(username, bucketSize);
        return true;
    }

}
