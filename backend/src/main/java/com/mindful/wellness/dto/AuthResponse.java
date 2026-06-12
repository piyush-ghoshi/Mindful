package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for authentication response.
 * 
 * Contains user information and authentication tokens returned after successful login or registration.
 * 
 * Validates: Requirements 1, 2, 3, 26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private UUID userId;

    private UserDto user;

    private String accessToken;

    private String refreshToken;

    private Long expiresIn;

    private String tokenType;

    private String message;
}
