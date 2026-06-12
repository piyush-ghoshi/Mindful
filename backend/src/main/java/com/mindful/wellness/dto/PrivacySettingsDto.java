package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for privacy settings.
 * 
 * Validates: Requirements 1, 2, 3, 25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivacySettingsDto {

    private String profileVisibility = "PRIVATE"; // PUBLIC, FRIENDS, PRIVATE

    private Boolean showMoodToForum = false;

    private Boolean allowCounsellorMoodAccess = true;

    private Boolean anonymousForumPosting = true;
}
