package com.mindful.wellness.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a personalized action recommendation.
 * Based on risk assessment, suggests specific interventions.
 */
@Entity
@Table(name = "action_recommendations", indexes = {
    @Index(name = "idx_action_user", columnList = "user_id"),
    @Index(name = "idx_action_session", columnList = "session_id"),
    @Index(name = "idx_action_risk", columnList = "risk_assessment_id"),
    @Index(name = "idx_action_priority", columnList = "priority"),
    @Index(name = "idx_action_status", columnList = "status"),
    @Index(name = "idx_action_type", columnList = "action_type"),
    @Index(name = "idx_action_user_status", columnList = "user_id, status"),
    @Index(name = "idx_action_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "session_id")
    private UUID sessionId;
    
    @Column(name = "risk_assessment_id")
    private UUID riskAssessmentId;
    
    // ── Recommendation Type ────────────────────────────────────────────────────
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private ActionType actionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private ActionPriority priority = ActionPriority.MEDIUM;
    
    // ── Recommendation Details ─────────────────────────────────────────────────
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "specific_action", columnDefinition = "TEXT")
    private String specificAction;
    
    // ── Resources ──────────────────────────────────────────────────────────────
    
    @Column(name = "resource_type", length = 50)
    private String resourceType; // EXERCISE, MEDITATION, ARTICLE, VIDEO, HELPLINE
    
    @Column(name = "resource_id")
    private UUID resourceId; // Foreign key to exercise/meditation tables
    
    @Column(name = "resource_url", length = 500)
    private String resourceUrl;
    
    // ── Implementation Tracking ────────────────────────────────────────────────
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ActionStatus status = ActionStatus.PENDING;
    
    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;
    
    // ── Metadata ───────────────────────────────────────────────────────────────
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ── Helper Methods ─────────────────────────────────────────────────────────
    
    /**
     * Check if this recommendation has expired.
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if this is a crisis-level recommendation.
     */
    public boolean isCritical() {
        return priority == ActionPriority.CRITICAL;
    }
    
    /**
     * Mark as viewed by user.
     */
    public void markViewed() {
        if (status == ActionStatus.PENDING) {
            status = ActionStatus.VIEWED;
            viewedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Mark as started by user.
     */
    public void markStarted() {
        if (status == ActionStatus.PENDING || status == ActionStatus.VIEWED) {
            status = ActionStatus.STARTED;
            startedAt = LocalDateTime.now();
            if (viewedAt == null) {
                viewedAt = LocalDateTime.now();
            }
        }
    }
    
    /**
     * Mark as completed by user.
     */
    public void markCompleted() {
        status = ActionStatus.COMPLETED;
        completedAt = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (viewedAt == null) {
            viewedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Mark as dismissed by user.
     */
    public void markDismissed() {
        status = ActionStatus.DISMISSED;
        dismissedAt = LocalDateTime.now();
    }
}
