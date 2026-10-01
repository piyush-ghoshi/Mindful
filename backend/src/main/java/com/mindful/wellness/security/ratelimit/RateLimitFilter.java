package com.mindful.wellness.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Servlet filter that enforces sliding-window rate limits across all API endpoints.
 *
 * Tiers:
 *   - /api/auth/** : 5 requests/minute per IP
 *   - /api/**      : 100 requests/minute per authenticated user (or per IP if anonymous)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only rate limit /api/** endpoints (skip /actuator, /error, static assets)
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Always allow health checks and crisis intervention endpoints without throttling
        if (path.equals("/api/health") || path.startsWith("/api/crisis")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);

        // 1. Strict rate limit on authentication endpoints (brute-force defense)
        if (path.startsWith("/api/auth/") || path.startsWith("/api/firebase-auth/")) {
            RateLimiterService.RateLimitResult authResult = rateLimiterService.checkAuthRateLimit(clientIp);
            setRateLimitHeaders(response, authResult);

            if (!authResult.isAllowed()) {
                writeRateLimitError(response, authResult.retryAfterSeconds(),
                        "Too many authentication attempts. Please try again in " + authResult.retryAfterSeconds() + " seconds.");
                return;
            }
        }

        // 2. General API rate limit (per user or per IP)
        String userIdentifier = resolveUserIdentifier(clientIp);
        RateLimiterService.RateLimitResult apiResult = rateLimiterService.checkApiRateLimit(userIdentifier);
        setRateLimitHeaders(response, apiResult);

        if (!apiResult.isAllowed()) {
            writeRateLimitError(response, apiResult.retryAfterSeconds(),
                    "API rate limit exceeded. Please try again in " + apiResult.retryAfterSeconds() + " seconds.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveUserIdentifier(String clientIp) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }
        return "ip:" + clientIp;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            // First IP in comma-separated list is the original client
            String[] ips = xForwardedFor.split(",");
            return ips[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private void setRateLimitHeaders(HttpServletResponse response, RateLimiterService.RateLimitResult result) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));
    }

    private void writeRateLimitError(HttpServletResponse response, long retryAfterSeconds, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        Map<String, Object> errorBody = Map.of(
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "error", "Too Many Requests",
                "message", message,
                "retryAfterSeconds", retryAfterSeconds
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
