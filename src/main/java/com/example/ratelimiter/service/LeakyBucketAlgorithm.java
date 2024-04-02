package com.example.ratelimiter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service("LeakyBucket")
public class LeakyBucketAlgorithm implements RateLimitingAlgorithm{

    private final int maxQueueSize;
    private final int requestsProcessedPerSecond;
    private final ConcurrentLinkedQueue<String> queue;

    LeakyBucketAlgorithm(@Value("${leakybucket.maxQueueSize}") int maxQueueSize,
                         @Value("${requestsProcessedPerSecond}") int requestsProcessedPerSecond) {
        this.maxQueueSize = maxQueueSize;
        this.requestsProcessedPerSecond = requestsProcessedPerSecond;
        queue = new ConcurrentLinkedQueue<>();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (queue.peek() != null) {
                    try {
                        System.out.println(queue.poll());
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        timer.schedule(timerTask, 0, requestsProcessedPerSecond*1000);
    }

    @Override
    public boolean isRequestAllowed(String username) {
        if (queue.size() >= maxQueueSize)
            return false;
        queue.add(username + LocalTime.now().toString());
        return true;
    }

}
