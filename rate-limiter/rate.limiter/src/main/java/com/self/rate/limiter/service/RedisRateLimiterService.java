package com.self.rate.limiter.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class RedisRateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final int capacity = 10;
    private final int refillRate = 1;

    public RedisRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean allowRequest(String userId) {

        String key = "rate_limit:" + userId;

        long now = Instant.now().getEpochSecond();

        String tokensStr = (String) redisTemplate.opsForHash().get(key, "tokens");
        String lastRefillStr = (String) redisTemplate.opsForHash().get(key, "last_refill");

        int tokens;
        long lastRefill;

        if (tokensStr == null) {
            tokens = capacity;
            lastRefill = now;
        } else {
            tokens = Integer.parseInt(tokensStr);
            lastRefill = Long.parseLong(lastRefillStr);
        }

        long timeElapsed = now - lastRefill;

        int tokensToAdd = (int) (timeElapsed * refillRate);

        if (tokensToAdd > 0) {
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefill = now;
        }

        if (tokens > 0) {
            tokens--;

            redisTemplate.opsForHash().put(key, "tokens", String.valueOf(tokens));
            redisTemplate.opsForHash().put(key, "last_refill", String.valueOf(lastRefill));

            redisTemplate.expire(key, 2, TimeUnit.MINUTES);

            return true;
        }

        return false;
    }
}
