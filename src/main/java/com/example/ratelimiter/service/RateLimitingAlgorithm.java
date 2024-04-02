package com.example.ratelimiter.service;

public interface RateLimitingAlgorithm {

     boolean isRequestAllowed(String username);
}
