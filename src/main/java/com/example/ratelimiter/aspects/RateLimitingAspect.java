package com.example.ratelimiter.aspects;

import com.example.ratelimiter.service.RateLimitingAlgorithm;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class RateLimitingAspect {

    @Qualifier("TokenBucket")
    @Autowired
    RateLimitingAlgorithm algorithm;

    @Pointcut("execution(* com.example.ratelimiter.controller.ApiController.*(..))")
    public void rateLimit(){}

    @Around("rateLimit()")
    public Object applyRateLimiting(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        String username = (String)args[0];
        if (algorithm.isRequestAllowed(username)) {
            return proceedingJoinPoint.proceed();
        }
        else {
            return new ResponseEntity<>("API Limit Reached", HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
