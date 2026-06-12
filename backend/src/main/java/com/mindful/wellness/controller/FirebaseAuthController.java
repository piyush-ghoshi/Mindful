package com.mindful.wellness.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.mindful.wellness.dto.*;
import com.mindful.wellness.service.FirebaseAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Firebase authentication endpoints.
 * 
 * Provides endpoints for:
 * - User registration with Firebase
 * - User login verification
 * - Token verification
 * - User profile management
 * 
 * Validates: Requirements 1, 2, 3, 26
 */
@Slf4j
@RestController
@RequestMapping("/api/firebase-auth")
@RequiredArgsConstructor
public class FirebaseAuthController {

    private final FirebaseAuthService firebaseAuthService;

    /**
     * Register a new user with Firebase.
     * 
     * Endpoint: POST /api/firebase-auth/register
     * 
     * @param registerRequest the registration request
     * @return ResponseEntity with AuthResponse containing user info
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            log.info("Firebase registration request received for email: {}", registerRequest.getEmail());
            AuthResponse authResponse = firebaseAuthService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (FirebaseAuthException e) {
            log.warn("Firebase registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Firebase registration failed: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (IllegalArgumentException e) {
            log.warn("Registration validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Registration failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Verify user login with Firebase.
     * Note: Password verification should be done on client-side using Firebase SDK.
     * This endpoint verifies the user exists and returns user information.
     * 
     * Endpoint: POST /api/firebase-auth/login
     * 
     * @param loginRequest the login request
     * @return ResponseEntity with AuthResponse containing user info
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Firebase login request received for email: {}", loginRequest.getEmail());
            AuthResponse authResponse = firebaseAuthService.login(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Login validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Login failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Verify Firebase ID token and get user information.
     * 
     * Endpoint: POST /api/firebase-auth/verify-token
     * 
     * @param tokenRequest the token verification request
     * @return ResponseEntity with user information if token is valid
     */
    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@Valid @RequestBody TokenVerificationRequest tokenRequest) {
        try {
            log.info("Token verification request received");
            var userRecord = firebaseAuthService.verifyIdToken(tokenRequest.getIdToken());
            
            var user = firebaseAuthService.getUserByFirebaseUid(userRecord.getUid());
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("User not found", HttpStatus.NOT_FOUND.value()));
            }

            return ResponseEntity.ok(new TokenVerificationResponse(
                    userRecord.getUid(),
                    userRecord.getEmail(),
                    userRecord.getDisplayName(),
                    user.get().getRole().toString(),
                    true
            ));
        } catch (FirebaseAuthException e) {
            log.warn("Token verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            log.error("Token verification error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Token verification failed", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Update user password in Firebase.
     * 
     * Endpoint: POST /api/firebase-auth/update-password
     * 
     * @param updatePasswordRequest the password update request
     * @return ResponseEntity with success message
     */
    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        try {
            log.info("Password update request received for UID: {}", updatePasswordRequest.getUid());
            firebaseAuthService.updatePassword(updatePasswordRequest.getUid(), updatePasswordRequest.getNewPassword());
            return ResponseEntity.ok(new SuccessResponse("Password updated successfully"));
        } catch (FirebaseAuthException e) {
            log.warn("Password update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Password update failed: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error("Password update error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Password update failed", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Disable user account.
     * 
     * Endpoint: POST /api/firebase-auth/disable-user
     * 
     * @param disableUserRequest the disable user request
     * @return ResponseEntity with success message
     */
    @PostMapping("/disable-user")
    public ResponseEntity<?> disableUser(@Valid @RequestBody DisableUserRequest disableUserRequest) {
        try {
            log.info("Disable user request received for UID: {}", disableUserRequest.getUid());
            firebaseAuthService.disableUser(disableUserRequest.getUid());
            return ResponseEntity.ok(new SuccessResponse("User disabled successfully"));
        } catch (FirebaseAuthException e) {
            log.warn("Disable user failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Disable user failed: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error("Disable user error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Disable user failed", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Enable user account.
     * 
     * Endpoint: POST /api/firebase-auth/enable-user
     * 
     * @param enableUserRequest the enable user request
     * @return ResponseEntity with success message
     */
    @PostMapping("/enable-user")
    public ResponseEntity<?> enableUser(@Valid @RequestBody EnableUserRequest enableUserRequest) {
        try {
            log.info("Enable user request received for UID: {}", enableUserRequest.getUid());
            firebaseAuthService.enableUser(enableUserRequest.getUid());
            return ResponseEntity.ok(new SuccessResponse("User enabled successfully"));
        } catch (FirebaseAuthException e) {
            log.warn("Enable user failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Enable user failed: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error("Enable user error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Enable user failed", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Delete user account.
     * 
     * Endpoint: DELETE /api/firebase-auth/delete-user/{uid}
     * 
     * @param uid the Firebase UID
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/delete-user/{uid}")
    public ResponseEntity<?> deleteUser(@PathVariable String uid) {
        try {
            log.info("Delete user request received for UID: {}", uid);
            firebaseAuthService.deleteUser(uid);
            return ResponseEntity.ok(new SuccessResponse("User deleted successfully"));
        } catch (FirebaseAuthException e) {
            log.warn("Delete user failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Delete user failed: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error("Delete user error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Delete user failed", HttpStatus.INTERNAL_SERVER_ERROR.value()));
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
     * Inner class for success responses.
     */
    public static class SuccessResponse {
        public String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Inner class for token verification response.
     */
    public static class TokenVerificationResponse {
        public String uid;
        public String email;
        public String displayName;
        public String role;
        public boolean valid;

        public TokenVerificationResponse(String uid, String email, String displayName, String role, boolean valid) {
            this.uid = uid;
            this.email = email;
            this.displayName = displayName;
            this.role = role;
            this.valid = valid;
        }

        public String getUid() {
            return uid;
        }

        public String getEmail() {
            return email;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getRole() {
            return role;
        }

        public boolean isValid() {
            return valid;
        }
    }
}
