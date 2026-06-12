package com.mindful.wellness.dto;

import com.mindful.wellness.entity.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Notification entity.
 * 
 * Used for API responses and requests related to notifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {

    private UUID id;
    private UUID userId;
    private String notificationType;
    private String title;
    private String message;
    private UUID relatedEntityId;
    private String relatedEntityType;
    private NotificationStatus status;
    private String channel;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime scheduledFor;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
