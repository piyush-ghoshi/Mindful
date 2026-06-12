package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for student profile information.
 * 
 * Validates: Requirements 1, 2, 3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileDto {

    private UUID id;

    private UUID userId;

    private String studentId;

    private UUID institutionId;

    private LocalDate dateOfBirth;

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
