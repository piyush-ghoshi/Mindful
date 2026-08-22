package com.mindful.wellness.repository;

import com.mindful.wellness.entity.ActionPriority;
import com.mindful.wellness.entity.ActionRecommendation;
import com.mindful.wellness.entity.ActionStatus;
import com.mindful.wellness.entity.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for ActionRecommendation entity.
 */
@Repository
public interface ActionRecommendationRepository extends JpaRepository<ActionRecommendation, UUID> {
    
    // ── Basic Queries ──────────────────────────────────────────────────────────
    
    List<ActionRecommendation> findByUserIdOrderByPriorityAscCreatedAtDesc(UUID userId);
    
    List<ActionRecommendation> findBySessionIdOrderByPriorityAscCreatedAtDesc(UUID sessionId);
    
    List<ActionRecommendation> findByRiskAssessmentId(UUID riskAssessmentId);
    
    // ── Status Queries ─────────────────────────────────────────────────────────
    
    List<ActionRecommendation> findByUserIdAndStatusOrderByPriorityAscCreatedAtDesc(
        UUID userId, ActionStatus status
    );
    
    /**
     * Get pending and started actions (active recommendations).
     */
    @Query("SELECT ar FROM ActionRecommendation ar WHERE ar.userId = :userId " +
           "AND ar.status IN ('PENDING', 'VIEWED', 'STARTED') " +
           "ORDER BY ar.priority ASC, ar.createdAt DESC")
    List<ActionRecommendation> findActiveRecommendations(@Param("userId") UUID userId);
    
    /**
     * Get critical priority actions.
     */
    List<ActionRecommendation> findByUserIdAndPriorityOrderByCreatedAtDesc(
        UUID userId, ActionPriority priority
    );
    
    /**
     * Get urgent actions (critical + high priority) that are still pending.
     */
    @Query("SELECT ar FROM ActionRecommendation ar WHERE ar.userId = :userId " +
           "AND ar.priority IN ('CRITICAL', 'HIGH') " +
           "AND ar.status IN ('PENDING', 'VIEWED', 'STARTED') " +
           "ORDER BY ar.priority ASC, ar.createdAt DESC")
    List<ActionRecommendation> findUrgentActions(@Param("userId") UUID userId);
    
    // ── Type Queries ───────────────────────────────────────────────────────────
    
    List<ActionRecommendation> findByUserIdAndActionTypeOrderByCreatedAtDesc(
        UUID userId, ActionType actionType
    );
    
    /**
     * Check if user already has a pending recommendation of this type.
     */
    @Query("SELECT COUNT(ar) > 0 FROM ActionRecommendation ar " +
           "WHERE ar.userId = :userId AND ar.actionType = :type " +
           "AND ar.status IN ('PENDING', 'VIEWED', 'STARTED')")
    boolean hasActiverecommendation(@Param("userId") UUID userId, @Param("type") ActionType type);
    
    // ── Time-based Queries ─────────────────────────────────────────────────────
    
    /**
     * Get recommendations created after a specific time.
     */
    List<ActionRecommendation> findByUserIdAndCreatedAtAfterOrderByPriorityAscCreatedAtDesc(
        UUID userId, LocalDateTime since
    );
    
    /**
     * Get expired recommendations that are still pending.
     */
    @Query("SELECT ar FROM ActionRecommendation ar WHERE ar.userId = :userId " +
           "AND ar.expiresAt < :now AND ar.status IN ('PENDING', 'VIEWED', 'STARTED')")
    List<ActionRecommendation> findExpiredActions(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    // ── Statistics ─────────────────────────────────────────────────────────────
    
    /**
     * Count recommendations by status for a user.
     */
    @Query("SELECT ar.status, COUNT(ar) FROM ActionRecommendation ar " +
           "WHERE ar.userId = :userId GROUP BY ar.status")
    List<Object[]> countByStatus(@Param("userId") UUID userId);
    
    /**
     * Count completed recommendations.
     */
    long countByUserIdAndStatus(UUID userId, ActionStatus status);
    
    /**
     * Get completion rate (completed / total).
     */
    @Query("SELECT " +
           "CAST(SUM(CASE WHEN ar.status = 'COMPLETED' THEN 1 ELSE 0 END) AS FLOAT) / COUNT(ar) " +
           "FROM ActionRecommendation ar WHERE ar.userId = :userId")
    Double getCompletionRate(@Param("userId") UUID userId);
    
    // ── Recent Activity ────────────────────────────────────────────────────────
    
    List<ActionRecommendation> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Get recently completed actions.
     */
    List<ActionRecommendation> findByUserIdAndStatusOrderByCompletedAtDesc(
        UUID userId, ActionStatus status
    );
}
