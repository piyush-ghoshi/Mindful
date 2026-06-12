package com.mindful.wellness.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Firebase ID token verification request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenVerificationRequest {

    @NotBlank(message = "ID token is required")
    private String idToken;
}
