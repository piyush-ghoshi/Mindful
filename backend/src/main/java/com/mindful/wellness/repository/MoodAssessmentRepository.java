package com.mindful.wellness.repository;

import com.mindful.wellness.entity.MoodAssessment;
import com.mindful.wellness.entity.MoodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MoodAssessment entity.
 * Provides queries for mood tracking, trajectory analysis, and pattern detection.
 */
@Repository
public interface MoodAssessmentRepository extends JpaRepository<MoodAssessment, UUID> {
    
    // ── Basic Queries ──────────────────────────────────────────────────────────
    
    /**
     * Find all mood assessments for a specific session, ordered chronologically.
     */
    List<MoodAssessment> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
    
    /**
     * Find all mood assessments for a specific user, ordered by most recent.
     */
    List<MoodAssessment> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find mood assessment for a specific message.
     */
    Optional<MoodAssessment> findByMessageId(UUID messageId);
    
    /**
     * Find all assessments for a user within a time range.
     */
    List<MoodAssessment> findByUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(
        UUID userId, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    // ── Mood Analysis Queries ──────────────────────────────────────────────────
    
    /**
     * Find recent assessments for a user (last N assessments).
     */
    List<MoodAssessment> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find assessments with a specific mood type for a user.
     */
    List<MoodAssessment> findByUserIdAndDetectedMoodOrderByCreatedAtDesc(
        UUID userId, 
        MoodType moodType
    );
    
    /**
     * Find high-intensity mood assessments (intensity >= threshold).
     */
    @Query("SELECT ma FROM MoodAssessment ma WHERE ma.sessionId = :sessionId " +
           "AND ma.moodIntensity >= :threshold ORDER BY ma.createdAt ASC")
    List<MoodAssessment> findHighIntensityMoodsBySession(
        @Param("sessionId") UUID sessionId,
        @Param("threshold") BigDecimal threshold
    );
    
    /**
     * Find concerning assessments (negative mood with high intensity).
     */
    @Query("SELECT ma FROM MoodAssessment ma WHERE ma.userId = :userId " +
           "AND ma.detectedMood IN ('SAD', 'ANXIOUS', 'HOPELESS', 'OVERWHELMED') " +
           "AND ma.moodIntensity >= 0.6 " +
           "ORDER BY ma.createdAt DESC")
    List<MoodAssessment> findConcerningMoodsByUser(@Param("userId") UUID userId);
    
    /**
     * Count assessments by mood type for a session.
     */
    @Query("SELECT ma.detectedMood, COUNT(ma) FROM MoodAssessment ma " +
           "WHERE ma.sessionId = :sessionId " +
           "GROUP BY ma.detectedMood")
    List<Object[]> countMoodTypesBySession(@Param("sessionId") UUID sessionId);
    
    // ── Sentiment Analysis Queries ─────────────────────────────────────────────
    
    /**
     * Calculate average sentiment score for a session.
     */
    @Query("SELECT AVG(ma.sentimentScore) FROM MoodAssessment ma " +
           "WHERE ma.sessionId = :sessionId")
    Optional<BigDecimal> calculateAverageSentimentForSession(@Param("sessionId") UUID sessionId);
    
    /**
     * Calculate average sentiment score for a user over time.
     */
    @Query("SELECT AVG(ma.sentimentScore) FROM MoodAssessment ma " +
           "WHERE ma.userId = :userId " +
           "AND ma.createdAt >= :since")
    Optional<BigDecimal> calculateAverageSentimentForUserSince(
        @Param("userId") UUID userId,
        @Param("since") LocalDateTime since
    );
    
    /**
     * Find assessments with very negative sentiment (< -0.5).
     */
    @Query("SELECT ma FROM MoodAssessment ma WHERE ma.userId = :userId " +
           "AND ma.sentimentScore < -0.5 " +
           "ORDER BY ma.createdAt DESC")
    List<MoodAssessment> findNegativeSentimentByUser(@Param("userId") UUID userId);
    
    // ── Trend Analysis Queries ─────────────────────────────────────────────────
    
    /**
     * Get mood trajectory for a user (sentiment over time).
     * Returns list of [timestamp, sentiment_score] pairs.
     */
    @Query("SELECT ma.createdAt, ma.sentimentScore FROM MoodAssessment ma " +
           "WHERE ma.userId = :userId " +
           "AND ma.createdAt >= :since " +
           "ORDER BY ma.createdAt ASC")
    List<Object[]> getMoodTrajectoryForUser(
        @Param("userId") UUID userId,
        @Param("since") LocalDateTime since
    );
    
    /**
     * Get daily mood averages for a user.
     */
    @Query("SELECT DATE(ma.createdAt), AVG(ma.sentimentScore), AVG(ma.moodIntensity) " +
           "FROM MoodAssessment ma " +
           "WHERE ma.userId = :userId " +
           "AND ma.createdAt >= :since " +
           "GROUP BY DATE(ma.createdAt) " +
           "ORDER BY DATE(ma.createdAt) ASC")
    List<Object[]> getDailyMoodAveragesForUser(
        @Param("userId") UUID userId,
        @Param("since") LocalDateTime since
    );
    
    // ── Statistics Queries ─────────────────────────────────────────────────────
    
    /**
     * Count total assessments for a user.
     */
    long countByUserId(UUID userId);
    
    /**
     * Count assessments for a session.
     */
    long countBySessionId(UUID sessionId);
    
    /**
     * Get most common mood for a user.
     */
    @Query("SELECT ma.detectedMood, COUNT(ma) as cnt FROM MoodAssessment ma " +
           "WHERE ma.userId = :userId " +
           "GROUP BY ma.detectedMood " +
           "ORDER BY cnt DESC")
    List<Object[]> getMostCommonMoodsByUser(@Param("userId") UUID userId);
    
    /**
     * Check if user has any concerning moods in recent sessions.
     */
    @Query("SELECT COUNT(ma) > 0 FROM MoodAssessment ma " +
           "WHERE ma.userId = :userId " +
           "AND ma.createdAt >= :since " +
           "AND ma.detectedMood IN ('HOPELESS', 'OVERWHELMED') " +
           "AND ma.moodIntensity >= 0.7")
    boolean hasConcerningMoodsRecently(
        @Param("userId") UUID userId,
        @Param("since") LocalDateTime since
    );
}
