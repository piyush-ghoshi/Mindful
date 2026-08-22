package com.mindful.wellness.repository;

import com.mindful.wellness.entity.RecommendedAction;
import com.mindful.wellness.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RiskAssessment entity.
 */
@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {
    
    // ── Basic Queries ──────────────────────────────────────────────────────────
    
    List<RiskAssessment> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
    
    List<RiskAssessment> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    Optional<RiskAssessment> findByMessageId(UUID messageId);
    
    List<RiskAssessment> findByUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(
        UUID userId, LocalDateTime start, LocalDateTime end
    );
    
    // ── Risk Level Queries ─────────────────────────────────────────────────────
    
    /**
     * Find high-risk assessments (score >= 7).
     */
    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.userId = :userId " +
           "AND ra.totalRiskScore >= 7 ORDER BY ra.createdAt DESC")
    List<RiskAssessment> findHighRiskAssessments(@Param("userId") UUID userId);
    
    /**
     * Find critical/crisis assessments (score >= 9).
     */
    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.userId = :userId " +
           "AND ra.totalRiskScore >= 9 ORDER BY ra.createdAt DESC")
    List<RiskAssessment> findCrisisAssessments(@Param("userId") UUID userId);
    
    /**
     * Get highest risk score for a session.
     */
    @Query("SELECT MAX(ra.totalRiskScore) FROM RiskAssessment ra WHERE ra.sessionId = :sessionId")
    Optional<Integer> getMaxRiskScoreForSession(@Param("sessionId") UUID sessionId);
    
    /**
     * Get average risk score for a user over time period.
     */
    @Query("SELECT AVG(ra.totalRiskScore) FROM RiskAssessment ra " +
           "WHERE ra.userId = :userId AND ra.createdAt >= :since")
    Optional<Double> getAverageRiskScore(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
    
    // ── Action Recommendations ─────────────────────────────────────────────────
    
    /**
     * Find assessments requiring urgent action.
     */
    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.userId = :userId " +
           "AND ra.recommendedAction IN ('CRISIS_INTERVENTION', 'URGENT_COUNSELLOR_ALERT', 'SAME_DAY_APPOINTMENT') " +
           "ORDER BY ra.createdAt DESC")
    List<RiskAssessment> findUrgentActions(@Param("userId") UUID userId);
    
    List<RiskAssessment> findByRecommendedActionOrderByCreatedAtDesc(RecommendedAction action);
    
    // ── Trend Analysis ─────────────────────────────────────────────────────────
    
    /**
     * Check if risk is increasing over time.
     */
    @Query("SELECT ra.totalRiskScore FROM RiskAssessment ra " +
           "WHERE ra.userId = :userId AND ra.createdAt >= :since " +
           "ORDER BY ra.createdAt ASC")
    List<Integer> getRiskScoreTrend(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
    
    /**
     * Count assessments by risk level.
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN ra.totalRiskScore <= 2 THEN 'MINIMAL' " +
           "  WHEN ra.totalRiskScore <= 4 THEN 'LOW' " +
           "  WHEN ra.totalRiskScore <= 6 THEN 'MODERATE' " +
           "  WHEN ra.totalRiskScore <= 8 THEN 'HIGH' " +
           "  ELSE 'SEVERE' " +
           "END as riskLevel, COUNT(ra) " +
           "FROM RiskAssessment ra WHERE ra.userId = :userId " +
           "GROUP BY riskLevel")
    List<Object[]> countByRiskLevel(@Param("userId") UUID userId);
    
    // ── Recent Activity ────────────────────────────────────────────────────────
    
    List<RiskAssessment> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Check if user has any recent high-risk assessments.
     */
    @Query("SELECT COUNT(ra) > 0 FROM RiskAssessment ra " +
           "WHERE ra.userId = :userId AND ra.totalRiskScore >= 7 " +
           "AND ra.createdAt >= :since")
    boolean hasRecentHighRisk(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
}
