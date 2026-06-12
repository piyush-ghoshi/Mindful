package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notification preferences.
 * 
 * Validates: Requirements 1, 2, 3, 27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferencesDto {

    private Boolean emailEnabled = true;

    private Boolean smsEnabled = false;

    private Boolean pushEnabled = true;

    private Boolean appointmentReminders = true;

    private Boolean messageNotifications = true;

    private Boolean wellnessReminders = true;

    private Boolean forumReplies = true;
}
