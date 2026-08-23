package com.mindful.wellness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a multi-factor risk assessment.
 * Evaluates suicide/self-harm risk based on multiple indicators.
 */
@Entity
@Table(name = "risk_assessments", indexes = {
    @Index(name = "idx_risk_session", columnList = "session_id"),
    @Index(name = "idx_risk_user", columnList = "user_id"),
    @Index(name = "idx_risk_score", columnList = "total_risk_score"),
    @Index(name = "idx_risk_created_at", columnList = "created_at"),
    @Index(name = "idx_risk_action", columnList = "recommended_action"),
    @Index(name = "idx_risk_user_time", columnList = "user_id, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;
    
    @Column(name = "message_id")
    private UUID messageId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    // ── Risk Scoring ───────────────────────────────────────────────────────────
    
    /**
     * Total risk score (0-10).
     * 0-2: Minimal, 3-4: Low, 5-6: Moderate, 7-8: High, 9-10: Severe/Critical
     */
    @Column(name = "total_risk_score", nullable = false)
    private Integer totalRiskScore;
    
    /**
     * Risk factors detected (JSON).
     * Example: {"suicidal_ideation": 10, "hopelessness": 7, "isolation": 5}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "risk_factors", columnDefinition = "jsonb")
    private String riskFactors;
    
    /**
     * Protective factors (JSON).
     * Example: {"social_support": true, "coping_skills": 0.6, "help_seeking": true}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "protective_factors", columnDefinition = "jsonb")
    private String protectiveFactors;
    
    // ── Analysis Metadata ──────────────────────────────────────────────────────
    
    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_method", length = 20)
    @Builder.Default
    private AnalysisMethod assessmentMethod = AnalysisMethod.HYBRID;
    
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;
    
    // ── Recommendations ────────────────────────────────────────────────────────
    
    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_action", length = 50)
    private RecommendedAction recommendedAction;
    
    @Column(name = "rationale", columnDefinition = "TEXT")
    private String rationale;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // ── Helper Methods ─────────────────────────────────────────────────────────
    
    /**
     * Get risk level based on score.
     */
    public RiskLevel getRiskLevel() {
        return RiskLevel.fromScore(totalRiskScore);
    }
    
    /**
     * Check if this assessment indicates a crisis.
     */
    public boolean isCrisis() {
        return totalRiskScore >= 9;
    }
    
    /**
     * Check if immediate action is required.
     */
    public boolean requiresImmediateAction() {
        return totalRiskScore >= 7;
    }
    
    /**
     * Get a human-readable description.
     */
    public String getDescription() {
        RiskLevel level = getRiskLevel();
        return String.format("Risk Level: %s (Score: %d/10) - %s", 
            level.getDisplayName(), 
            totalRiskScore,
            level.getDescription());
    }
}
