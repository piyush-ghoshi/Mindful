package com.mindful.wellness.repository;

import com.mindful.wellness.entity.AvailabilitySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AvailabilitySchedule entity providing database access operations.
 * 
 * Validates: Requirement 13
 */
@Repository
public interface AvailabilityScheduleRepository extends JpaRepository<AvailabilitySchedule, UUID> {

    /**
     * Find all availability schedules for a counsellor.
     * 
     * @param counsellorId the counsellor ID
     * @return list of availability schedules for the counsellor
     */
    List<AvailabilitySchedule> findByCounsellorId(UUID counsellorId);

    /**
     * Find availability schedule for a counsellor on a specific day of week.
     * 
     * @param counsellorId the counsellor ID
     * @param dayOfWeek the day of week
     * @return Optional containing the availability schedule if found
     */
    Optional<AvailabilitySchedule> findByCounsellorIdAndDayOfWeek(UUID counsellorId, DayOfWeek dayOfWeek);

    /**
     * Find all available schedules for a counsellor (where isAvailable is true).
     * 
     * @param counsellorId the counsellor ID
     * @return list of available schedules
     */
    @Query("SELECT a FROM AvailabilitySchedule a WHERE a.counsellorId = :counsellorId AND a.isAvailable = true")
    List<AvailabilitySchedule> findAvailableSchedulesByCounsellorId(@Param("counsellorId") UUID counsellorId);

    /**
     * Check if a schedule exists for a counsellor on a specific day.
     * 
     * @param counsellorId the counsellor ID
     * @param dayOfWeek the day of week
     * @return true if schedule exists
     */
    boolean existsByCounsellorIdAndDayOfWeek(UUID counsellorId, DayOfWeek dayOfWeek);

    /**
     * Delete all schedules for a counsellor.
     * 
     * @param counsellorId the counsellor ID
     */
    void deleteByCounsellorId(UUID counsellorId);
}
