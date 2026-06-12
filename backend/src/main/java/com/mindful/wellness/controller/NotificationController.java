package com.mindful.wellness.controller;

import com.mindful.wellness.dto.NotificationDto;
import com.mindful.wellness.entity.Notification;
import com.mindful.wellness.service.NotificationService;
import com.mindful.wellness.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for notification management endpoints.
 * Validates: Requirement 27
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthUtil authUtil;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getUserNotifications(Authentication authentication) {
        try {
            UUID userId = authUtil.getUserId(authentication);
            List<NotificationDto> dtos = notificationService.getUserNotifications(userId)
                    .stream().map(this::convertToDto).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error retrieving notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(Authentication authentication) {
        try {
            UUID userId = authUtil.getUserId(authentication);
            List<NotificationDto> dtos = notificationService.getUnreadNotifications(userId)
                    .stream().map(this::convertToDto).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error retrieving unread notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadNotificationCount(Authentication authentication) {
        try {
            UUID userId = authUtil.getUserId(authentication);
            return ResponseEntity.ok(notificationService.getUnreadNotificationCount(userId));
        } catch (Exception e) {
            log.error("Error retrieving unread notification count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getNotification(
            @PathVariable UUID id, Authentication authentication) {
        try {
            UUID userId = authUtil.getUserId(authentication);
            Notification notification = notificationService.getNotificationById(id);
            if (!notification.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(convertToDto(notification));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving notification {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markNotificationAsRead(
            @PathVariable UUID id, Authentication authentication) {
        try {
            UUID userId = authUtil.getUserId(authentication);
            Notification notification = notificationService.getNotificationById(id);
            if (!notification.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            notificationService.markNotificationAsRead(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error marking notification {} as read", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private NotificationDto convertToDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .notificationType(n.getNotificationType())
                .title(n.getTitle())
                .message(n.getMessage())
                .relatedEntityId(n.getRelatedEntityId())
                .relatedEntityType(n.getRelatedEntityType())
                .status(n.getStatus())
                .channel(n.getChannel())
                .sentAt(n.getSentAt())
                .readAt(n.getReadAt())
                .scheduledFor(n.getScheduledFor())
                .retryCount(n.getRetryCount())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
