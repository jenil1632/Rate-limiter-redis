package com.example.ratelimiter.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Service("SlidingWindowCounter")
public class SlidingWindowCounterAlgorithm implements RateLimitingAlgorithm{

    private final Map<LocalDateTime, Integer> windowMap;
    private final int maxAllowedRequests;
    private final int slidingWindowSizeInMilliseconds;
    private LocalDateTime currentWindowStartTime;
    private LocalDateTime previousWindowStartTime;

    SlidingWindowCounterAlgorithm() {
        windowMap = new HashMap<>();
        maxAllowedRequests = 5;
        slidingWindowSizeInMilliseconds = 10000;
        currentWindowStartTime = LocalDateTime.now();
        previousWindowStartTime = currentWindowStartTime.minus(slidingWindowSizeInMilliseconds, ChronoUnit.MILLIS);
        windowMap.put(currentWindowStartTime, 0);
        windowMap.put(previousWindowStartTime, 0);

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                LocalDateTime currentTime = LocalDateTime.now();
                previousWindowStartTime = currentWindowStartTime;
                currentWindowStartTime = currentTime;
                windowMap.put(currentWindowStartTime, 0);
            }
        };

        timer.schedule(timerTask, slidingWindowSizeInMilliseconds, slidingWindowSizeInMilliseconds);
    }

    @Override
    public boolean isRequestAllowed(String username) {
        LocalDateTime currentTime = LocalDateTime.now();
        int currentWindowRequestCount = windowMap.get(currentWindowStartTime);
        int previousWindowRequestCount = windowMap.get(previousWindowStartTime);
        long currDuration = Duration.between(currentWindowStartTime.toInstant(ZoneOffset.UTC), currentTime.toInstant(ZoneOffset.UTC)).toMillis();
        long prevDuration = slidingWindowSizeInMilliseconds - currDuration;
        long reqCount = (currDuration*currentWindowRequestCount + prevDuration*previousWindowRequestCount)/slidingWindowSizeInMilliseconds;
        if (reqCount >= maxAllowedRequests)
            return false;
        windowMap.computeIfPresent(currentWindowStartTime, (k,v) -> v+1);
        return true;
    }
}
