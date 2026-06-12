package com.mindful.wellness.entity;

/**
 * Enumeration for notification status.
 * 
 * Represents the lifecycle state of a notification.
 */
public enum NotificationStatus {
    PENDING,    // Notification created but not yet sent
    SENT,       // Notification successfully sent
    FAILED,     // Notification failed to send
    READ        // Notification has been read by user
}
