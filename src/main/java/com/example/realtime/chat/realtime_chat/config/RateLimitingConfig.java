package com.example.realtime.chat.realtime_chat.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitingConfig {
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    private RateLimiter resolveLimiter(String key, int limit, Duration period) {
        return limiters.computeIfAbsent(key, k -> RateLimiter.of(k, RateLimiterConfig.custom()
                .limitForPeriod(limit)
                .limitRefreshPeriod(period)
                .timeoutDuration(Duration.ofMillis(0))
                .build()));
    }

    @Bean
    public OncePerRequestFilter rateLimitingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain filterChain)
                    throws jakarta.servlet.ServletException, IOException {
                String ip = request.getRemoteAddr();
                String path = request.getRequestURI();
                // Global rate limit: 100 requests/minute per IP
                RateLimiter globalLimiter = resolveLimiter("global:" + ip, 100, Duration.ofMinutes(1));
                // Login rate limit: 5 requests/minute per IP
                RateLimiter loginLimiter = resolveLimiter("login:" + ip, 5, Duration.ofMinutes(1));
                // File upload rate limit: 10 requests/minute per IP
                RateLimiter uploadLimiter = resolveLimiter("upload:" + ip, 10, Duration.ofMinutes(1));

                boolean allowed = true;
                if (path.contains("/login")) {
                    allowed = globalLimiter.acquirePermission() && loginLimiter.acquirePermission();
                } else if (path.contains("/api/upload")) {
                    allowed = globalLimiter.acquirePermission() && uploadLimiter.acquirePermission();
                } else {
                    allowed = globalLimiter.acquirePermission();
                }
                if (!allowed) {
                    response.setStatus(429);
                    response.getWriter().write("Rate limit exceeded");
                    return;
                }
                filterChain.doFilter(request, response);
            }
        };
    }
} 