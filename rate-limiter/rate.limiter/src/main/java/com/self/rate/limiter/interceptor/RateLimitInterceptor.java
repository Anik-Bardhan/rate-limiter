package com.self.rate.limiter.interceptor;

import com.self.rate.limiter.service.RedisRateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public record RateLimitInterceptor(RedisRateLimiterService rateLimiterService) implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String userId = request.getHeader("X-USER-ID");

        if (userId == null) {
            response.setStatus(400);
            response.getWriter().write("Missing X-USER-ID header");
            return false;
        }

        boolean allowed = rateLimiterService.allowRequest(userId);

        if (!allowed) {
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return false;
        }

        return true;
    }

}
