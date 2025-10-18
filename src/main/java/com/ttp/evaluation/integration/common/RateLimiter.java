package com.ttp.evaluation.integration.common;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiter для внешних API
 */
@Component
@Slf4j
public class RateLimiter {

    private final RateLimiterRegistry registry;
    private final ConcurrentHashMap<String, io.github.resilience4j.ratelimiter.RateLimiter> limiters;

    public RateLimiter() {
        this.registry = RateLimiterRegistry.ofDefaults();
        this.limiters = new ConcurrentHashMap<>();

        // Конфигурация лимитов для различных API
        createRateLimiter("uncomtrade", 1, Duration.ofSeconds(3));   // 100 запросов в час
        createRateLimiter("emiss", 1, Duration.ofSeconds(3));       // 30 запросов в минуту (ЕМИСС)
        createRateLimiter("rosstat", 1, Duration.ofSeconds(3));     // 30 запросов в минуту
        createRateLimiter("wto", 1, Duration.ofSeconds(3));         // 30 запросов в минуту (снижен до 30)
    }

    private void createRateLimiter(String name, int limitForPeriod, Duration limitRefreshPeriod) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(limitForPeriod)
                .limitRefreshPeriod(limitRefreshPeriod)
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        io.github.resilience4j.ratelimiter.RateLimiter rateLimiter =
                registry.rateLimiter(name, config);

        limiters.put(name, rateLimiter);
        log.info("✅ Created rate limiter for {}: {} requests per {}",
                name, limitForPeriod, limitRefreshPeriod);
    }

    public void acquire(String apiName) {
        io.github.resilience4j.ratelimiter.RateLimiter limiter = limiters.get(apiName);
        if (limiter != null) {
            try {
                limiter.acquirePermission();
            } catch (Exception e) {
                log.warn("⚠️ Rate limit exceeded for API: {}", apiName);
                throw e;
            }
        } else {
            log.warn("⚠️ No rate limiter configured for API: {}", apiName);
        }
    }
}
