package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a pair of JWT tokens.
 * 
 * Contains access token and refresh token with expiration information.
 * 
 * Validates: Requirements 1, 2, 3, 26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenPair {

    private String accessToken;

    private String refreshToken;

    private Long expiresIn;

    private String tokenType;
}
