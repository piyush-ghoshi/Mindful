package com.mindful.wellness.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import com.mindful.wellness.dto.AuthResponse;
import com.mindful.wellness.dto.LoginRequest;
import com.mindful.wellness.dto.RegisterRequest;
import com.mindful.wellness.dto.UserDto;
import com.mindful.wellness.entity.User;
import com.mindful.wellness.entity.UserRole;
import com.mindful.wellness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Firebase Authentication Service.
 * Handles user registration, login, and authentication using Firebase Admin SDK.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseAuthService {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;

    /**
     * Register a new user with Firebase and create a local user record.
     *
     * @param registerRequest Registration request containing email, password, and user details
     * @return AuthResponse with user information
     * @throws FirebaseAuthException if Firebase registration fails
     */
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) throws FirebaseAuthException {
        try {
            // Check if user already exists in local database
            if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
                throw new IllegalArgumentException("User with this email already exists");
            }

            // Create user in Firebase
            CreateRequest createRequest = new CreateRequest()
                    .setEmail(registerRequest.getEmail())
                    .setPassword(registerRequest.getPassword())
                    .setDisplayName(registerRequest.getFullName())
                    .setDisabled(false);

            UserRecord userRecord = firebaseAuth.createUser(createRequest);
            log.info("Firebase user created with UID: {}", userRecord.getUid());

            // Create local user record
            User user = new User();
            user.setFirebaseUid(userRecord.getUid());
            user.setEmail(registerRequest.getEmail());
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setPhoneNumber(registerRequest.getPhoneNumber());
            user.setLanguagePreference(registerRequest.getLanguagePreference());
            
            // Set role - default to STUDENT if not provided
            String roleStr = registerRequest.getRole() != null ? registerRequest.getRole().toUpperCase() : "STUDENT";
            try {
                user.setRole(UserRole.valueOf(roleStr));
            } catch (IllegalArgumentException e) {
                user.setRole(UserRole.STUDENT);
            }
            
            user.setIsActive(true);
            user.setPasswordHash(""); // Firebase handles password, so we don't store it locally

            User savedUser = userRepository.save(user);
            log.info("Local user record created for email: {}", registerRequest.getEmail());

            return AuthResponse.builder()
                    .userId(savedUser.getId())
                    .user(convertToUserDto(savedUser))
                    .message("User registered successfully")
                    .build();

        } catch (FirebaseAuthException e) {
            log.error("Firebase registration error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Authenticate user with Firebase using email and password.
     * Note: Firebase Admin SDK doesn't directly authenticate with password.
     * This method verifies the user exists and returns user information.
     * Client-side authentication should be done using Firebase SDK.
     *
     * @param loginRequest Login request containing email and password
     * @return AuthResponse with user information
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Get user by email from Firebase
            UserRecord userRecord = firebaseAuth.getUserByEmail(loginRequest.getEmail());

            // Get local user record
            Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

            if (userOptional.isEmpty()) {
                throw new IllegalArgumentException("User not found in local database");
            }

            User user = userOptional.get();

            if (!user.getIsActive()) {
                throw new IllegalArgumentException("User account is inactive");
            }

            log.info("User login successful for email: {}", loginRequest.getEmail());

            return AuthResponse.builder()
                    .userId(user.getId())
                    .user(convertToUserDto(user))
                    .message("Login successful")
                    .build();

        } catch (FirebaseAuthException e) {
            log.error("Firebase login error: {}", e.getMessage());
            throw new RuntimeException("Login failed: User not found");
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    /**
     * Verify Firebase ID token.
     *
     * @param idToken Firebase ID token from client
     * @return UserRecord if token is valid
     * @throws FirebaseAuthException if token is invalid
     */
    public UserRecord verifyIdToken(String idToken) throws FirebaseAuthException {
        try {
            var decodedToken = firebaseAuth.verifyIdToken(idToken);
            String uid = decodedToken.getUid();
            return firebaseAuth.getUser(uid);
        } catch (FirebaseAuthException e) {
            log.error("Token verification failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get user by Firebase UID.
     *
     * @param uid Firebase UID
     * @return User if found
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByFirebaseUid(String uid) {
        return userRepository.findByFirebaseUid(uid);
    }

    /**
     * Update user password in Firebase.
     *
     * @param uid Firebase UID
     * @param newPassword New password
     * @throws FirebaseAuthException if update fails
     */
    public void updatePassword(String uid, String newPassword) throws FirebaseAuthException {
        try {
            UpdateRequest updateRequest = new UpdateRequest(uid)
                    .setPassword(newPassword);
            firebaseAuth.updateUser(updateRequest);
            log.info("Password updated for user: {}", uid);
        } catch (FirebaseAuthException e) {
            log.error("Password update failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Disable user account in Firebase.
     *
     * @param uid Firebase UID
     * @throws FirebaseAuthException if disable fails
     */
    @Transactional
    public void disableUser(String uid) throws FirebaseAuthException {
        try {
            UpdateRequest updateRequest = new UpdateRequest(uid)
                    .setDisabled(true);
            firebaseAuth.updateUser(updateRequest);

            // Update local user record
            Optional<User> userOptional = userRepository.findByFirebaseUid(uid);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setIsActive(false);
                userRepository.save(user);
            }

            log.info("User disabled: {}", uid);
        } catch (FirebaseAuthException e) {
            log.error("User disable failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Enable user account in Firebase.
     *
     * @param uid Firebase UID
     * @throws FirebaseAuthException if enable fails
     */
    @Transactional
    public void enableUser(String uid) throws FirebaseAuthException {
        try {
            UpdateRequest updateRequest = new UpdateRequest(uid)
                    .setDisabled(false);
            firebaseAuth.updateUser(updateRequest);

            // Update local user record
            Optional<User> userOptional = userRepository.findByFirebaseUid(uid);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setIsActive(true);
                userRepository.save(user);
            }

            log.info("User enabled: {}", uid);
        } catch (FirebaseAuthException e) {
            log.error("User enable failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Delete user from Firebase and local database.
     *
     * @param uid Firebase UID
     * @throws FirebaseAuthException if deletion fails
     */
    @Transactional
    public void deleteUser(String uid) throws FirebaseAuthException {
        try {
            firebaseAuth.deleteUser(uid);

            // Delete local user record
            Optional<User> userOptional = userRepository.findByFirebaseUid(uid);
            if (userOptional.isPresent()) {
                userRepository.delete(userOptional.get());
            }

            log.info("User deleted: {}", uid);
        } catch (FirebaseAuthException e) {
            log.error("User deletion failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Convert User entity to UserDto.
     *
     * @param user the user entity
     * @return UserDto with non-sensitive user information
     */
    private UserDto convertToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .languagePreference(user.getLanguagePreference())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
