package com.mindful.wellness.repository;

import com.mindful.wellness.entity.AvailabilityException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AvailabilityException entity providing database access operations.
 * 
 * Validates: Requirement 13
 */
@Repository
public interface AvailabilityExceptionRepository extends JpaRepository<AvailabilityException, UUID> {

    /**
     * Find an availability exception for a counsellor on a specific date.
     * 
     * @param counsellorId the counsellor ID
     * @param exceptionDate the exception date
     * @return Optional containing the availability exception if found
     */
    Optional<AvailabilityException> findByCounsellorIdAndExceptionDate(UUID counsellorId, LocalDate exceptionDate);

    /**
     * Find all availability exceptions for a counsellor within a date range.
     * 
     * @param counsellorId the counsellor ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of availability exceptions within the date range
     */
    @Query("SELECT a FROM AvailabilityException a WHERE a.counsellorId = :counsellorId " +
           "AND a.exceptionDate >= :startDate AND a.exceptionDate <= :endDate " +
           "ORDER BY a.exceptionDate ASC")
    List<AvailabilityException> findByCounsellorIdAndExceptionDateBetween(
            @Param("counsellorId") UUID counsellorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find all availability exceptions for a counsellor.
     * 
     * @param counsellorId the counsellor ID
     * @return list of all availability exceptions for the counsellor
     */
    List<AvailabilityException> findByCounsellorId(UUID counsellorId);

    /**
     * Find all unavailable exceptions for a counsellor (where isAvailable is false).
     * 
     * @param counsellorId the counsellor ID
     * @return list of unavailable exceptions
     */
    @Query("SELECT a FROM AvailabilityException a WHERE a.counsellorId = :counsellorId AND a.isAvailable = false")
    List<AvailabilityException> findUnavailableExceptionsByCounsellorId(@Param("counsellorId") UUID counsellorId);

    /**
     * Check if an exception exists for a counsellor on a specific date.
     * 
     * @param counsellorId the counsellor ID
     * @param exceptionDate the exception date
     * @return true if exception exists
     */
    boolean existsByCounsellorIdAndExceptionDate(UUID counsellorId, LocalDate exceptionDate);

    /**
     * Delete all exceptions for a counsellor.
     * 
     * @param counsellorId the counsellor ID
     */
    void deleteByCounsellorId(UUID counsellorId);
}
