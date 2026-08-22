package com.mindful.wellness.repository;

import com.mindful.wellness.entity.CrisisIntervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CrisisInterventionRepository extends JpaRepository<CrisisIntervention, UUID> {
    
    List<CrisisIntervention> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    List<CrisisIntervention> findBySessionId(UUID sessionId);
    
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.userId = :userId " +
           "AND ci.status IN ('INITIATED', 'IN_PROGRESS') " +
           "ORDER BY ci.createdAt DESC")
    List<CrisisIntervention> findActiveInterventions(@Param("userId") UUID userId);
    
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.userId = :userId " +
           "AND ci.severityLevel >= 9 ORDER BY ci.createdAt DESC")
    List<CrisisIntervention> findCriticalCrises(@Param("userId") UUID userId);
    
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.followUpRequired = true " +
           "AND ci.followUpDate <= :date ORDER BY ci.followUpDate ASC")
    List<CrisisIntervention> findDueForFollowUp(@Param("date") LocalDateTime date);
    
    long countByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime since);
}
