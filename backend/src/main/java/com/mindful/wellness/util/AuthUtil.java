package com.mindful.wellness.util;

import com.mindful.wellness.entity.User;
import com.mindful.wellness.entity.UserRole;
import com.mindful.wellness.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility for resolving the authenticated user's UUID from the security context.
 *
 * The JWT subject is the user's email. Controllers must look up the UUID via this helper
 * rather than trying to parse authentication.getName() as a UUID directly.
 */
@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final UserRepository userRepository;
    private final HttpServletRequest request;

    /**
     * Return the UUID of the currently authenticated user.
     *
     * @param authentication Spring Security authentication object
     * @return the user's UUID
     * @throws IllegalArgumentException if the user cannot be found
     */
    public UUID getUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found: " + email));
        return user.getId();
    }

    /**
     * Return the full User entity for the currently authenticated user.
     */
    public User getUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found: " + email));
    }

    // ── Convenience methods that pull from SecurityContextHolder ──────────────

    /**
     * Return the UUID of the currently authenticated user without requiring
     * an explicit Authentication parameter.
     */
    public UUID getCurrentUserId() {
        return getUserId(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Return the full User entity for the currently authenticated user without
     * requiring an explicit Authentication parameter.
     */
    public User getCurrentUser() {
        return getUser(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Return the role of the currently authenticated user.
     * Checks the X-User-Role header first (sent by frontend on role switch).
     */
    public UserRole getCurrentUserRole() {
        try {
            if (request != null) {
                String headerRole = request.getHeader("X-User-Role");
                if (headerRole != null && !headerRole.trim().isEmpty()) {
                    return UserRole.valueOf(headerRole.trim().toUpperCase());
                }
            }
        } catch (Exception ignored) {
            // Safe fallback if request context is not active (e.g. background tasks or tests)
        }
        return getCurrentUser().getRole();
    }
}
