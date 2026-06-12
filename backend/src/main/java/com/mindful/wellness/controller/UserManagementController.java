package com.mindful.wellness.controller;

import com.mindful.wellness.dto.*;
import com.mindful.wellness.entity.User;
import com.mindful.wellness.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for user management endpoints.
 * 
 * Provides endpoints for retrieving and updating user profiles, managing counsellor information,
 * and handling student profile operations.
 * 
 * Validates: Requirements 1, 2, 3, 6, 12, 13, 15, 25, 26
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {

    private final UserManagementService userManagementService;

    /**
     * Get user profile by ID.
     * 
     * @param id the user ID
     * @return the user profile
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable String id) {
        try {
            UUID userId = resolveUserId(id);
            User user = userManagementService.getUserById(userId);
            UserProfileDto profileDto = convertUserToProfileDto(user);
            return ResponseEntity.ok(profileDto);
        } catch (IllegalArgumentException e) {
            log.warn("User not found for identifier: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update user profile.
     * 
     * @param id the user ID
     * @param request the profile update request
     * @return the updated user profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileDto> updateUserProfile(
            @PathVariable String id,
            @RequestBody ProfileUpdateRequest request) {
        try {
            UUID userId = resolveUserId(id);
            User updatedUser = userManagementService.updateUserProfile(userId, request);
            UserProfileDto profileDto = convertUserToProfileDto(updatedUser);
            return ResponseEntity.ok(profileDto);
        } catch (IllegalArgumentException e) {
            log.warn("User not found for identifier: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update user language preference.
     * 
     * @param id the user ID
     * @param language the language preference
     * @return the updated user profile
     */
    @PutMapping("/{id}/language")
    public ResponseEntity<UserProfileDto> updateLanguagePreference(
            @PathVariable String id,
            @RequestParam String language) {
        try {
            UUID userId = resolveUserId(id);
            User updatedUser = userManagementService.updateLanguagePreference(userId, language);
            UserProfileDto profileDto = convertUserToProfileDto(updatedUser);
            return ResponseEntity.ok(profileDto);
        } catch (IllegalArgumentException e) {
            log.warn("User not found for identifier: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get list of available counsellors.
     * Returns all users with COUNSELLOR role — no counsellor_profiles join required.
     */
    @GetMapping("/counsellors")
    public ResponseEntity<List<CounsellorProfileDto>> getCounsellors(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) Boolean acceptingNewStudents,
            @RequestParam(required = false) Double minRating) {

        // Query users table directly by role — works even without counsellor_profiles rows
        List<com.mindful.wellness.entity.User> counsellorUsers =
                userManagementService.getUsersByRole(com.mindful.wellness.entity.UserRole.COUNSELLOR);

        List<CounsellorProfileDto> result = counsellorUsers.stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .map(u -> CounsellorProfileDto.builder()
                        .id(u.getId())
                        .userId(u.getId())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .email(u.getEmail())
                        .isAcceptingNewStudents(true)
                        .rating(0.0)
                        .totalAppointments(0)
                        .build())
                .collect(java.util.stream.Collectors.toList());

        // Enrich with counsellor_profiles data if available
        try {
            CounsellorFiltersDto filters = CounsellorFiltersDto.builder()
                    .institutionId(institutionId)
                    .acceptingNewStudents(acceptingNewStudents)
                    .minRating(minRating)
                    .build();
            List<CounsellorProfileDto> profileData = userManagementService.getCounsellors(filters);
            if (!profileData.isEmpty()) {
                return ResponseEntity.ok(profileData);
            }
        } catch (Exception ignored) { /* fall through to basic list */ }

        return ResponseEntity.ok(result);
    }

    /**
     * Get counsellor details by ID.
     * 
     * @param id the counsellor ID
     * @return the counsellor profile
     */
    @GetMapping("/counsellors/{id}")
    public ResponseEntity<CounsellorProfileDto> getCounsellorById(@PathVariable String id) {
        try {
            UUID userId = resolveUserId(id);
            CounsellorProfileDto counsellor = userManagementService.getCounsellorById(userId);
            return ResponseEntity.ok(counsellor);
        } catch (IllegalArgumentException e) {
            log.warn("Counsellor not found for identifier: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update counsellor availability schedule.
     * 
     * @param id the counsellor ID
     * @param availability the new availability schedule
     * @return success response
     */
    @PutMapping("/counsellors/{id}/availability")
    public ResponseEntity<Void> updateCounsellorAvailability(
            @PathVariable String id,
            @RequestBody CounsellorProfileDto profile) {
        try {
            UUID userId = resolveUserId(id);
            boolean success = userManagementService.updateCounsellorProfile(userId, profile);
            if (success) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (IllegalArgumentException e) {
            log.warn("Counsellor not found for identifier: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get student profile by student ID.
     * 
     * @param id the student ID
     * @return the student profile
     */
    @GetMapping("/students/{id}/profile")
    public ResponseEntity<StudentProfileDto> getStudentProfile(@PathVariable String id) {
        try {
            UUID userId = resolveUserId(id);
            StudentProfileDto studentProfile = userManagementService.getStudentProfile(userId);
            return ResponseEntity.ok(studentProfile);
        } catch (IllegalArgumentException e) {
            log.warn("Student profile not found for identifier: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Helper to resolve path variable String to user's UUID.
     * Tries to parse as UUID first; if invalid, resolves by Firebase UID lookup.
     */
    private UUID resolveUserId(String idStr) {
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            return userManagementService.getUserByFirebaseUid(idStr)
                    .map(User::getId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with Firebase UID: " + idStr));
        }
    }

    /**
     * Convert User entity to UserProfileDto.
     * 
     * @param user the user entity
     * @return the user profile DTO
     */
    private UserProfileDto convertUserToProfileDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .languagePreference(user.getLanguagePreference())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
