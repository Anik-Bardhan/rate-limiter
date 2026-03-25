package com.self.rate.limiter.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Service
public class RedisLuaRateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> rateLimiterScript;

    public RedisLuaRateLimiterService(StringRedisTemplate redisTemplate,
                                   DefaultRedisScript<Long> rateLimiterScript) {
        this.redisTemplate = redisTemplate;
        this.rateLimiterScript = rateLimiterScript;
    }

    public boolean allowRequest(String userId) {

        String key = "rate_limit:" + userId;

        Long result = redisTemplate.execute(
                rateLimiterScript,
                Collections.singletonList(key),
                "10",
                "1",
                String.valueOf(Instant.now().getEpochSecond())
        );

        return result != null && result == 1;
    }

}
