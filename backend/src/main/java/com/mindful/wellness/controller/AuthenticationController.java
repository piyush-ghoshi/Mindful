package com.mindful.wellness.controller;

import com.mindful.wellness.dto.*;
import com.mindful.wellness.security.exception.AuthenticationException;
import com.mindful.wellness.service.AuthenticationService;
import com.mindful.wellness.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints.
 * 
 * Provides endpoints for:
 * - User registration
 * - User login
 * - Token refresh
 * - User logout
 * 
 * Validates: Requirements 1, 2, 3, 26
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * Register a new user.
     * 
     * Endpoint: POST /api/auth/register
     * 
     * @param registerRequest the registration request
     * @return ResponseEntity with AuthResponse containing user info and tokens
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            log.info("Registration request received for email: {}", registerRequest.getEmail());
            AuthResponse authResponse = authenticationService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (AuthenticationException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Registration failed", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Authenticate user and generate tokens.
     * 
     * Endpoint: POST /api/auth/login
     * 
     * @param loginRequest the login request
     * @return ResponseEntity with AuthResponse containing user info and tokens
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login request received for email: {}", loginRequest.getEmail());
            AuthResponse authResponse = authenticationService.login(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) {
            log.warn("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Login failed", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Refresh access token using refresh token.
     * 
     * Endpoint: POST /api/auth/refresh
     * 
     * @param refreshTokenRequest the refresh token request
     * @return ResponseEntity with new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            log.info("Token refresh request received");
            
            // Validate refresh token
            if (!authenticationService.validateRefreshToken(refreshTokenRequest.getRefreshToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid refresh token", HttpStatus.UNAUTHORIZED.value()));
            }

            // Generate new access token
            String username = authenticationService.getUsernameFromRefreshToken(refreshTokenRequest.getRefreshToken());
            String newAccessToken = authenticationService.generateAccessToken(username);

            return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, "Bearer"));
        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Token refresh failed", HttpStatus.UNAUTHORIZED.value()));
        }
    }

    /**
     * Logout user by invalidating session.
     * 
     * Endpoint: POST /api/auth/logout
     * 
     * @return ResponseEntity with logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            log.info("Logout request received");
            
            // Clear security context
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(new LogoutResponse("Logout successful"));
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Logout failed", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Get current authenticated user information.
     * 
     * Endpoint: GET /api/auth/me
     * 
     * @return ResponseEntity with current user information
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("User not authenticated", HttpStatus.UNAUTHORIZED.value()));
            }

            log.info("Current user request for: {}", authentication.getName());
            return ResponseEntity.ok(new CurrentUserResponse(authentication.getName(), authentication.getAuthorities().toString()));
        } catch (Exception e) {
            log.error("Error fetching current user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch current user", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Initiate password reset — sends a reset link to the given email.
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Email is required", 400));
        }
        // Always return 200 to prevent email enumeration
        passwordResetService.initiatePasswordReset(email.trim());
        return ResponseEntity.ok(java.util.Map.of("message", "If that email is registered, a reset link has been sent."));
    }

    /**
     * Complete password reset using the token from the email link.
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody java.util.Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Token and newPassword are required", 400));
        }
        try {
            passwordResetService.resetPassword(token.trim(), newPassword);
            return ResponseEntity.ok(java.util.Map.of("message", "Password reset successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), 400));
        }
    }

    /**
     * Inner class for error responses.
     */
    public static class ErrorResponse {
        public String message;
        public int status;

        public ErrorResponse(String message, int status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public int getStatus() {
            return status;
        }
    }

    /**
     * Inner class for logout response.
     */
    public static class LogoutResponse {
        public String message;

        public LogoutResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Inner class for token refresh response.
     */
    public static class TokenRefreshResponse {
        public String accessToken;
        public String tokenType;

        public TokenRefreshResponse(String accessToken, String tokenType) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }
    }

    /**
     * Inner class for current user response.
     */
    public static class CurrentUserResponse {
        public String username;
        public String authorities;

        public CurrentUserResponse(String username, String authorities) {
            this.username = username;
            this.authorities = authorities;
        }

        public String getUsername() {
            return username;
        }

        public String getAuthorities() {
            return authorities;
        }
    }
}
