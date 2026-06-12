package com.mindful.wellness.repository;

import com.mindful.wellness.entity.Notification;
import com.mindful.wellness.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Notification entity.
 * 
 * Provides database access for notification operations including
 * retrieval, filtering, and status updates.
 * 
 * Validates: Requirement 27
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find all notifications for a user.
     * 
     * @param userId the user ID
     * @return list of notifications
     */
    List<Notification> findByUserId(UUID userId);

    /**
     * Find all unread notifications for a user.
     * 
     * @param userId the user ID
     * @return list of unread notifications
     */
    List<Notification> findByUserIdAndStatus(UUID userId, NotificationStatus status);

    /**
     * Find all pending notifications that need to be sent.
     * 
     * @return list of pending notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' " +
           "AND (n.scheduledFor IS NULL OR n.scheduledFor <= CURRENT_TIMESTAMP) " +
           "ORDER BY n.createdAt ASC")
    List<Notification> findPendingNotifications();

    /**
     * Find all failed notifications that need retry.
     * 
     * @param maxRetries maximum number of retries
     * @return list of failed notifications eligible for retry
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' " +
           "AND n.retryCount < :maxRetries " +
           "ORDER BY n.updatedAt ASC")
    List<Notification> findFailedNotificationsForRetry(@Param("maxRetries") int maxRetries);

    /**
     * Find notifications by type for a user.
     * 
     * @param userId the user ID
     * @param notificationType the notification type
     * @return list of notifications of the specified type
     */
    List<Notification> findByUserIdAndNotificationType(UUID userId, String notificationType);

    /**
     * Find notifications related to a specific entity.
     * 
     * @param relatedEntityId the related entity ID
     * @param relatedEntityType the related entity type
     * @return list of related notifications
     */
    List<Notification> findByRelatedEntityIdAndRelatedEntityType(UUID relatedEntityId, String relatedEntityType);

    /**
     * Count unread notifications for a user.
     * 
     * @param userId the user ID
     * @return count of unread notifications
     */
    long countByUserIdAndStatus(UUID userId, NotificationStatus status);

    /**
     * Find notifications created within a date range.
     * 
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of notifications within the date range
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
           "AND n.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find notifications by channel.
     * 
     * @param userId the user ID
     * @param channel the notification channel
     * @return list of notifications sent via the specified channel
     */
    List<Notification> findByUserIdAndChannel(UUID userId, String channel);
}
