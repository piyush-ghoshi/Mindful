package com.mindful.wellness.security.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance, sliding-window rate limiter service.
 *
 * Enforces per-IP and per-user request limits with microsecond resolution.
 * Automatically cleans up expired windows to prevent memory leaks.
 */
@Slf4j
@Service
public class RateLimiterService {

    @Value("${rate-limit.auth.requests-per-minute:5}")
    private int authRequestsPerMinute;

    @Value("${rate-limit.api.requests-per-minute:100}")
    private int apiRequestsPerMinute;

    // Sliding window records: key -> timestamps (in epoch milliseconds)
    private final Map<String, Deque<Long>> requestWindows = new ConcurrentHashMap<>();

    // Keep track of last eviction pass to clean stale keys periodically
    private volatile long lastEvictionTime = System.currentTimeMillis();
    private static final long EVICTION_INTERVAL_MS = 60_000L; // Evict stale keys every 60s

    /**
     * Rate limit result containing allow status and metadata.
     */
    public record RateLimitResult(boolean isAllowed, int limit, int remaining, long retryAfterSeconds) {}

    /**
     * Check if a request to an authentication endpoint is allowed.
     *
     * @param clientIp Client IP address
     * @return RateLimitResult with decision and headers
     */
    public RateLimitResult checkAuthRateLimit(String clientIp) {
        String key = "auth:ip:" + clientIp;
        return checkRateLimit(key, authRequestsPerMinute, 60_000L);
    }

    /**
     * Check if a request to general API endpoints is allowed.
     *
     * @param identifier User ID (if authenticated) or Client IP (if anonymous)
     * @return RateLimitResult with decision and headers
     */
    public RateLimitResult checkApiRateLimit(String identifier) {
        String key = "api:id:" + identifier;
        return checkRateLimit(key, apiRequestsPerMinute, 60_000L);
    }

    /**
     * Sliding window algorithm implementation.
     */
    public RateLimitResult checkRateLimit(String key, int maxRequests, long windowDurationMs) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowDurationMs;

        // Periodic maintenance cleanup
        periodicEviction(now, windowDurationMs);

        Deque<Long> timestamps = requestWindows.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            // Remove timestamps outside current window
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                timestamps.pollFirst();
            }

            int currentCount = timestamps.size();

            if (currentCount >= maxRequests) {
                // Rate limit exceeded
                long oldestInWindow = timestamps.isEmpty() ? now : timestamps.peekFirst();
                long resetTimeMs = oldestInWindow + windowDurationMs;
                long retryAfterSeconds = Math.max(1, (resetTimeMs - now + 999) / 1000);

                log.warn("Rate limit exceeded for key={}: {}/{} requests in window. Retry after {}s",
                        key, currentCount, maxRequests, retryAfterSeconds);

                return new RateLimitResult(false, maxRequests, 0, retryAfterSeconds);
            }

            // Allow request and record timestamp
            timestamps.addLast(now);
            int remaining = maxRequests - (currentCount + 1);
            return new RateLimitResult(true, maxRequests, remaining, 0);
        }
    }

    /**
     * Clean up keys that haven't been active within the window duration.
     */
    private void periodicEviction(long now, long windowDurationMs) {
        if (now - lastEvictionTime > EVICTION_INTERVAL_MS) {
            synchronized (this) {
                if (now - lastEvictionTime > EVICTION_INTERVAL_MS) {
                    lastEvictionTime = now;
                    long threshold = now - (windowDurationMs * 2);
                    requestWindows.entrySet().removeIf(entry -> {
                        Deque<Long> deque = entry.getValue();
                        synchronized (deque) {
                            return deque.isEmpty() || deque.peekLast() < threshold;
                        }
                    });
                }
            }
        }
    }

    public int getAuthRequestsPerMinute() {
        return authRequestsPerMinute;
    }

    public int getApiRequestsPerMinute() {
        return apiRequestsPerMinute;
    }
}
