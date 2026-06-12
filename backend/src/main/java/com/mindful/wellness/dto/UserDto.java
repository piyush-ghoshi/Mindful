package com.mindful.wellness.dto;

import com.mindful.wellness.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for user information.
 * 
 * Contains non-sensitive user information for API responses.
 * 
 * Validates: Requirements 1, 2, 3, 25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private UUID id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String profilePictureUrl;

    private UserRole role;

    private Boolean isActive;

    private Boolean isEmailVerified;

    private String languagePreference;

    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;
}
