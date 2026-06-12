package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for updating user profile information.
 * 
 * Validates: Requirements 1, 2, 3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateRequest {

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String profilePictureUrl;

    private String languagePreference;

    // Student-specific fields
    private String gender;

    private String emergencyContactName;

    private String emergencyContactPhone;

    private Boolean consentForDataSharing;

    private Boolean consentForAnonymousAnalytics;

    private String preferredCounsellorGender;

    private List<String> wellnessGoals;

    private NotificationPreferencesDto notificationPreferences;

    private PrivacySettingsDto privacySettings;
}
