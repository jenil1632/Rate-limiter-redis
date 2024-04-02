package com.example.ratelimiter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;

@RestController
public class ApiController {

    @GetMapping("/api/{username}")
    public ResponseEntity<String> printHelloMessage(@PathVariable String username) {
        LocalTime time = LocalTime.now();
        ResponseEntity<String> response = new ResponseEntity<>("Hello" + time.toString(), HttpStatus.OK);
        System.out.println(response.getBody() + " " + Thread.currentThread().getName());
        return response;
    }
}
