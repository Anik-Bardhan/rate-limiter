package com.self.rate.limiter.config;

import com.self.rate.limiter.interceptor.RateLimitInterceptor;
import com.self.rate.limiter.service.RedisRateLimiterService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RedisRateLimiterService rateLimiterService;

    public WebConfig(RedisRateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(
                new RateLimitInterceptor(rateLimiterService)
        ).addPathPatterns("/api/**");
    }

}
