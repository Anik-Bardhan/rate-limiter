package com.self.rate.limiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisLuaConfig {

    @Bean
    public DefaultRedisScript<Long> rateLimiterScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/tokenBucket.lua"));
        return script;
    }

}
