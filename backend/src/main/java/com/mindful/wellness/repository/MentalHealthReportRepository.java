package com.mindful.wellness.repository;

import com.mindful.wellness.entity.MentalHealthReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MentalHealthReportRepository extends JpaRepository<MentalHealthReport, UUID> {

    List<MentalHealthReport> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<MentalHealthReport> findBySessionId(UUID sessionId);

    /** Next phase number = max existing + 1 (or 0 if none) */
    @Query("SELECT COALESCE(MAX(r.phaseNumber), -1) + 1 FROM MentalHealthReport r WHERE r.userId = :userId")
    int nextPhaseNumber(@Param("userId") UUID userId);
}
