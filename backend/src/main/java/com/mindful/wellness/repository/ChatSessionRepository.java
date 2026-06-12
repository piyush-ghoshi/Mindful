package com.mindful.wellness.repository;

import com.mindful.wellness.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    /** Active session for user (there should be at most one) */
    Optional<ChatSession> findByUserIdAndIsActiveTrue(UUID userId);

    /** All sessions for user, newest first */
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /** Count sessions created today for rate limiting */
    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.userId = :userId AND s.createdAt >= :startOfDay")
    long countTodaysSessions(@Param("userId") UUID userId, @Param("startOfDay") LocalDateTime startOfDay);

    /** Count reports generated today for rate limiting */
    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.userId = :userId AND s.reportGenerated = true AND s.createdAt >= :startOfDay")
    long countTodaysReports(@Param("userId") UUID userId, @Param("startOfDay") LocalDateTime startOfDay);
}
