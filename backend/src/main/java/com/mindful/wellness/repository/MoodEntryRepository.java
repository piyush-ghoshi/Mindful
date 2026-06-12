package com.mindful.wellness.repository;

import com.mindful.wellness.entity.MoodEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for MoodEntry entity.
 * 
 * Provides database access methods for mood entries including
 * retrieval by student, date range filtering, and trend analysis.
 * 
 * Validates: Requirements 4, 15
 */
@Repository
public interface MoodEntryRepository extends JpaRepository<MoodEntry, UUID> {

    /**
     * Find all mood entries for a student.
     * 
     * @param studentId the student ID
     * @return list of mood entries
     */
    List<MoodEntry> findByStudentId(UUID studentId);

    /**
     * Find mood entries for a student within a date range with pagination.
     * 
     * @param studentId the student ID
     * @param startTime the start time
     * @param endTime the end time
     * @param pageable pagination parameters
     * @return page of mood entries within the date range
     */
    Page<MoodEntry> findByStudentIdAndRecordedAtBetweenOrderByRecordedAtDesc(
            UUID studentId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable);

    /**
     * Find mood entries for a student within a date range without pagination.
     * 
     * @param studentId the student ID
     * @param startTime the start time
     * @param endTime the end time
     * @return list of mood entries within the date range
     */
    List<MoodEntry> findByStudentIdAndRecordedAtBetweenOrderByRecordedAtDesc(
            UUID studentId,
            LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * Find the most recent mood entry for a student.
     * 
     * @param studentId the student ID
     * @return the most recent mood entry
     */
    @Query("SELECT m FROM MoodEntry m WHERE m.studentId = :studentId " +
            "ORDER BY m.recordedAt DESC LIMIT 1")
    MoodEntry findMostRecentByStudentId(@Param("studentId") UUID studentId);

    /**
     * Find mood entries for a student with a specific mood rating.
     * 
     * @param studentId the student ID
     * @param moodRating the mood rating
     * @return list of mood entries with the specified rating
     */
    List<MoodEntry> findByStudentIdAndMoodRating(UUID studentId, Integer moodRating);

    /**
     * Count mood entries for a student within a date range.
     * 
     * @param studentId the student ID
     * @param startTime the start time
     * @param endTime the end time
     * @return count of mood entries
     */
    @Query("SELECT COUNT(m) FROM MoodEntry m WHERE m.studentId = :studentId " +
            "AND m.recordedAt BETWEEN :startTime AND :endTime")
    long countByStudentIdAndDateRange(
            @Param("studentId") UUID studentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Calculate average mood rating for a student within a date range.
     * 
     * @param studentId the student ID
     * @param startTime the start time
     * @param endTime the end time
     * @return average mood rating
     */
    @Query("SELECT AVG(m.moodRating) FROM MoodEntry m WHERE m.studentId = :studentId " +
            "AND m.recordedAt BETWEEN :startTime AND :endTime")
    Double getAverageMoodRating(
            @Param("studentId") UUID studentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Calculate average energy level for a student within a date range.
     * 
     * @param studentId the student ID
     * @param startTime the start time
     * @param endTime the end time
     * @return average energy level
     */
    @Query("SELECT AVG(m.energyLevel) FROM MoodEntry m WHERE m.studentId = :studentId " +
            "AND m.recordedAt BETWEEN :startTime AND :endTime")
    Double getAverageEnergyLevel(
            @Param("studentId") UUID studentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Calculate average sleep quality for a student within a date range.
     * 
     * @param studentId the student ID
     * @param startTime the start time
     * @param endTime the end time
     * @return average sleep quality
     */
    @Query("SELECT AVG(m.sleepQuality) FROM MoodEntry m WHERE m.studentId = :studentId " +
            "AND m.recordedAt BETWEEN :startTime AND :endTime")
    Double getAverageSleepQuality(
            @Param("studentId") UUID studentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find mood entries for a student with low mood ratings (concerning patterns).
     * 
     * @param studentId the student ID
     * @param maxRating the maximum rating threshold
     * @param startTime the start time
     * @param endTime the end time
     * @return list of concerning mood entries
     */
    @Query("SELECT m FROM MoodEntry m WHERE m.studentId = :studentId " +
            "AND m.moodRating <= :maxRating " +
            "AND m.recordedAt BETWEEN :startTime AND :endTime " +
            "ORDER BY m.recordedAt DESC")
    List<MoodEntry> findConcerningMoodEntries(
            @Param("studentId") UUID studentId,
            @Param("maxRating") Integer maxRating,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find mood entries for a student with high sentiment scores (positive).
     * 
     * @param studentId the student ID
     * @param minSentiment the minimum sentiment threshold
     * @param startTime the start time
     * @param endTime the end time
     * @return list of positive mood entries
     */
    @Query("SELECT m FROM MoodEntry m WHERE m.studentId = :studentId " +
            "AND m.sentimentScore >= :minSentiment " +
            "AND m.recordedAt BETWEEN :startTime AND :endTime " +
            "ORDER BY m.recordedAt DESC")
    List<MoodEntry> findPositiveMoodEntries(
            @Param("studentId") UUID studentId,
            @Param("minSentiment") Double minSentiment,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
