package com.mindful.wellness.security.jwt;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.mindful.wellness.entity.User;
import com.mindful.wellness.entity.UserRole;
import com.mindful.wellness.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Filter that validates Firebase ID tokens sent as Bearer tokens.
 *
 * When the frontend authenticates via Firebase (email/password or Google),
 * it stores the Firebase ID token and sends it as "Authorization: Bearer <token>".
 * This filter:
 *   1. Tries to verify the token with Firebase Admin SDK.
 *   2. If valid, looks up (or auto-creates) the local User row.
 *   3. Sets Spring Security authentication so all @PreAuthorize checks work.
 *
 * The existing JwtAuthenticationFilter runs first; if it already set authentication
 * (i.e. the token was a local HS512 JWT), this filter skips processing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip if authentication is already set (local JWT was valid)
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String bearerToken = request.getHeader("Authorization");
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = bearerToken.substring(7);

        try {
            FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken);
            String email = decoded.getEmail();
            String uid   = decoded.getUid();
            String name  = decoded.getName() != null ? decoded.getName() : "";

            if (email == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Look up or auto-create the local user row
            // Respect X-User-Role header ONLY during first sign-up (auto-creation)
            String requestedRole = request.getHeader("X-User-Role");
            UserRole roleToAssign = UserRole.STUDENT;
            if (requestedRole != null) {
                try { roleToAssign = UserRole.valueOf(requestedRole.toUpperCase()); } catch (Exception ignored) {}
            }
            final UserRole finalRole = roleToAssign;

            User user;
            try {
                user = userRepository.findByEmail(email).orElseGet(() -> {
                    String[] parts = name.split(" ", 2);
                    String firstName = (parts.length > 0 && !parts[0].isBlank()) ? parts[0] : "User";
                    String lastName  = (parts.length > 1 && !parts[1].isBlank()) ? parts[1] : firstName;

                    User newUser = User.builder()
                            .email(email)
                            .username(email)
                            .firebaseUid(uid)
                            .firstName(firstName)
                            .lastName(lastName)
                            .passwordHash("FIREBASE_AUTH")
                            .role(finalRole)
                            .isActive(true)
                            .isEmailVerified(decoded.isEmailVerified())
                            .languagePreference("en")
                            .build();
                    return userRepository.save(newUser);
                });
            } catch (Exception e) {
                log.warn("Failed to auto-create user for uid={} email={} due to potential race condition, retrying lookup: {}", uid, email, e.getMessage());
                // Retry lookup to see if another concurrent thread created it
                user = userRepository.findByEmail(email).orElseThrow(() -> e);
            }

            // Sync firebaseUid if missing (single field update, batched with lastLogin below)
            boolean needsSave = false;
            if (user.getFirebaseUid() == null) {
                user.setFirebaseUid(uid);
                needsSave = true;
            }

            // Update last login only if it has been more than 15 minutes since last update (prevent write storm)
            LocalDateTime now = LocalDateTime.now();
            if (user.getLastLoginAt() == null || user.getLastLoginAt().isBefore(now.minusMinutes(15))) {
                user.setLastLoginAt(now);
                needsSave = true;
            }

            if (needsSave) {
                userRepository.save(user);
            }

            // Load full UserDetails (includes authorities)
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.debug("Firebase token authenticated for user: {}", email);

        } catch (FirebaseAuthException e) {
            // Token is invalid or expired — let the request continue unauthenticated
            // (Spring Security will return 401 for protected endpoints)
            log.debug("Firebase token validation failed: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing Firebase token", e);
        }

        filterChain.doFilter(request, response);
    }
}
