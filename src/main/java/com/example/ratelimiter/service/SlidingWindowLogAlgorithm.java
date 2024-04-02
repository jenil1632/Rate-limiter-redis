package com.example.ratelimiter.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("SlidingWindowLog")
public class SlidingWindowLogAlgorithm implements RateLimitingAlgorithm{

    private final Map<String, List<LocalDateTime>> logMap;
    private final int maxRequestsAllowed;
    private final int slidingWindowSizeInMilliseconds;

    SlidingWindowLogAlgorithm() {
        logMap = new HashMap<>();
        maxRequestsAllowed = 5;
        slidingWindowSizeInMilliseconds = 10000;
    }

    @Override
    public boolean isRequestAllowed(String username) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime cutOffTime = currentTime.minus(slidingWindowSizeInMilliseconds, ChronoUnit.MILLIS);
        if (logMap.containsKey(username)) {
            List<LocalDateTime> logs = logMap.get(username).stream().filter(cutOffTime::isBefore).collect(Collectors.toList());
            logs.add(currentTime);
            logMap.replace(username, logs);
            return logs.size() <= maxRequestsAllowed;
        }
        else {
            List<LocalDateTime> logs = new ArrayList<>();
            logs.add(currentTime);
            logMap.put(username, logs);
            return true;
        }
    }
}
