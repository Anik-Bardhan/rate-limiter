package com.self.rate.limiter.service;

import com.self.rate.limiter.ratelimiter.TokenBucket;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean allowRequest(String userId) {
        TokenBucket bucket = buckets.computeIfAbsent(
                userId, id -> new TokenBucket(10, 1)
        );
        return bucket.allowRequest();
    }

}
