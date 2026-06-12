package com.mindful.wellness.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for enable user request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnableUserRequest {

    @NotBlank(message = "Firebase UID is required")
    private String uid;
}
