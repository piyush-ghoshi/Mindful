package com.mindful.wellness.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI-generated mental health assessment report.
 * Stored per user, linked to a ChatSession.
 * Phase numbering is per-user (Phase 0, Phase 1, …).
 */
@Entity
@Table(name = "mental_health_reports", indexes = {
    @Index(name = "idx_reports_user_id", columnList = "user_id"),
    @Index(name = "idx_reports_created_at", columnList = "created_at")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MentalHealthReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "session_id")
    private UUID sessionId;

    /** Phase 0, Phase 1, … (auto-incremented per user) */
    @Column(name = "phase_number", nullable = false)
    private Integer phaseNumber;

    /** "Phase 0 — Initial Assessment" */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /** LOW / MODERATE / HIGH / SEVERE */
    @Column(name = "mental_state_level", nullable = false, length = 20)
    private String mentalStateLevel;

    /** 0–100 (0=severe, 100=excellent) */
    @Column(name = "wellness_score", nullable = false)
    private Integer wellnessScore;

    /** 5 JSON-array bullet points about condition */
    @Column(name = "condition_points", columnDefinition = "TEXT")
    private String conditionPoints;

    /** Recommended exercises JSON array */
    @Column(name = "recommended_exercises", columnDefinition = "TEXT")
    private String recommendedExercises;

    /** Recommended meditations JSON array */
    @Column(name = "recommended_meditations", columnDefinition = "TEXT")
    private String recommendedMeditations;

    /** 2-3 sentence conclusion */
    @Column(name = "conclusion", columnDefinition = "TEXT")
    private String conclusion;

    /** Whether counsellor referral was suggested */
    @Column(name = "counsellor_referral_suggested", nullable = false)
    private Boolean counsellorReferralSuggested = false;

    /** Full report JSON blob (for re-rendering) */
    @Column(name = "report_json", columnDefinition = "TEXT")
    private String reportJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
