package com.example.ratelimiter.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@Service("FixedTimeWindow")
public class FixedTimeWindowAlgorithm implements RateLimitingAlgorithm {

    private final Map<String, Integer> windowMap;
    private final int maxRequestsInWindow;
    private final long windowSizeInMilliseconds;

    FixedTimeWindowAlgorithm() {
        windowMap = new ConcurrentHashMap<>();
        windowSizeInMilliseconds = 10000;
        maxRequestsInWindow = 5;

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                windowMap.keySet().forEach(key-> windowMap.put(key, 0));
            }
        };
        timer.schedule(timerTask, 0, windowSizeInMilliseconds);
    }
    @Override
    public boolean isRequestAllowed(String username) {
        Integer val;
        val = windowMap.computeIfPresent(username, (k, v)-> v > maxRequestsInWindow? v : v+1);
        if (Objects.nonNull(val) && val <= maxRequestsInWindow) {
            return true;
        } else if (Objects.nonNull(val) && val > maxRequestsInWindow) {
            return false;
        }
        val = windowMap.putIfAbsent(username, 1);
        return Objects.isNull(val);
    }
}
