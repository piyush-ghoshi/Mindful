package com.mindful.wellness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a crisis intervention event.
 * Triggered when user is at high risk of self-harm or suicide.
 */
@Entity
@Table(name = "crisis_interventions", indexes = {
    @Index(name = "idx_crisis_user", columnList = "user_id"),
    @Index(name = "idx_crisis_session", columnList = "session_id"),
    @Index(name = "idx_crisis_risk", columnList = "risk_assessment_id"),
    @Index(name = "idx_crisis_type", columnList = "crisis_type"),
    @Index(name = "idx_crisis_status", columnList = "status"),
    @Index(name = "idx_crisis_severity", columnList = "severity_level"),
    @Index(name = "idx_crisis_created_at", columnList = "created_at"),
    @Index(name = "idx_crisis_follow_up", columnList = "follow_up_required, follow_up_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrisisIntervention {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "session_id")
    private UUID sessionId;
    
    @Column(name = "risk_assessment_id")
    private UUID riskAssessmentId;
    
    @Column(name = "message_id")
    private UUID messageId;
    
    // ── Crisis Details ─────────────────────────────────────────────────────────
    
    @Column(name = "crisis_type", nullable = false, length = 50)
    private String crisisType; // SUICIDAL_IDEATION, SELF_HARM, SEVERE_DISTRESS, SUBSTANCE_CRISIS
    
    @Column(name = "severity_level", nullable = false)
    private Integer severityLevel; // 0-10 scale
    
    @Column(name = "trigger_reason", columnDefinition = "TEXT")
    private String triggerReason;
    
    // ── Intervention ───────────────────────────────────────────────────────────
    
    @Column(name = "intervention_type", nullable = false, length = 50)
    private String interventionType; // AUTO_HELPLINE, COUNSELLOR_ALERT, EMERGENCY_CONTACT, SAFETY_PLAN
    
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "INITIATED"; // INITIATED, IN_PROGRESS, RESOLVED, ESCALATED
    
    // ── Actions Taken ──────────────────────────────────────────────────────────
    
    @Column(name = "helpline_provided")
    @Builder.Default
    private Boolean helplineProvided = false;
    
    @Column(name = "emergency_contacted")
    @Builder.Default
    private Boolean emergencyContacted = false;
    
    @Column(name = "counsellor_alerted")
    @Builder.Default
    private Boolean counsellorAlerted = false;
    
    @Column(name = "safety_plan_created")
    @Builder.Default
    private Boolean safetyPlanCreated = false;
    
    // ── Contact Attempts ───────────────────────────────────────────────────────
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contact_attempts", columnDefinition = "jsonb")
    private String contactAttempts; // JSON log
    
    // ── Resolution ─────────────────────────────────────────────────────────────
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    @Column(name = "follow_up_required")
    @Builder.Default
    private Boolean followUpRequired = true;
    
    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;
    
    // ── Metadata ───────────────────────────────────────────────────────────────
    
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
     * Check if this is a critical crisis (severity >= 9).
     */
    public boolean isCritical() {
        return severityLevel != null && severityLevel >= 9;
    }
    
    /**
     * Check if intervention is still active.
     */
    public boolean isActive() {
        return "INITIATED".equals(status) || "IN_PROGRESS".equals(status);
    }
    
    /**
     * Mark as resolved.
     */
    public void markResolved(String notes) {
        this.status = "RESOLVED";
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = notes;
    }
    
    /**
     * Mark as escalated to higher level of care.
     */
    public void markEscalated() {
        this.status = "ESCALATED";
    }
}
