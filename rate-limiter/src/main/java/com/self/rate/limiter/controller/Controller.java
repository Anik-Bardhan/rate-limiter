package com.self.rate.limiter.controller;

import com.self.rate.limiter.service.RateLimiterService;
import com.self.rate.limiter.service.RedisLuaRateLimiterService;
import com.self.rate.limiter.service.RedisRateLimiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Controller {

    @GetMapping("/resource")
    public String getResource() {
        return "Request allowed";
    }
}
