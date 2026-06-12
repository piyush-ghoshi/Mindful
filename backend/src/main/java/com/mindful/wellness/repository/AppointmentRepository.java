package com.mindful.wellness.repository;

import com.mindful.wellness.entity.Appointment;
import com.mindful.wellness.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Appointment entity providing database access operations.
 * 
 * Provides methods for:
 * - Finding appointments by student or counsellor
 * - Filtering appointments by status
 * - Querying appointments by time range
 * - Finding upcoming appointments
 * - Complex queries for appointment management
 * 
 * Validates: Requirements 6, 13
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    /**
     * Find all appointments for a student.
     * 
     * @param studentId the student ID
     * @return list of appointments for the student
     */
    List<Appointment> findByStudentId(UUID studentId);

    /**
     * Find all appointments for a counsellor.
     * 
     * @param counsellorId the counsellor ID
     * @return list of appointments for the counsellor
     */
    List<Appointment> findByCounsellorId(UUID counsellorId);

    /**
     * Find appointments for a student with a specific status.
     * 
     * @param studentId the student ID
     * @param status the appointment status
     * @return list of appointments matching the criteria
     */
    List<Appointment> findByStudentIdAndStatus(UUID studentId, AppointmentStatus status);

    /**
     * Find appointments for a counsellor with a specific status.
     * 
     * @param counsellorId the counsellor ID
     * @param status the appointment status
     * @return list of appointments matching the criteria
     */
    List<Appointment> findByCounsellorIdAndStatus(UUID counsellorId, AppointmentStatus status);

    /**
     * Find appointments within a time range.
     * 
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of appointments within the time range
     */
    List<Appointment> findByScheduledStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find appointments for a student within a time range.
     * 
     * @param studentId the student ID
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of appointments matching the criteria
     */
    @Query("SELECT a FROM Appointment a WHERE a.studentId = :studentId " +
           "AND a.scheduledStartTime BETWEEN :startTime AND :endTime " +
           "ORDER BY a.scheduledStartTime ASC")
    List<Appointment> findStudentAppointmentsByTimeRange(
            @Param("studentId") UUID studentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find appointments for a counsellor within a time range.
     * 
     * @param counsellorId the counsellor ID
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of appointments matching the criteria
     */
    @Query("SELECT a FROM Appointment a WHERE a.counsellorId = :counsellorId " +
           "AND a.scheduledStartTime BETWEEN :startTime AND :endTime " +
           "ORDER BY a.scheduledStartTime ASC")
    List<Appointment> findCounsellorAppointmentsByTimeRange(
            @Param("counsellorId") UUID counsellorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find upcoming appointments for a user (student or counsellor).
     * Includes SCHEDULED, CONFIRMED, and IN_PROGRESS statuses.
     * 
     * @param userId the user ID (can be student or counsellor)
     * @param now the current time
     * @return list of upcoming appointments ordered by scheduled start time
     */
    @Query("SELECT a FROM Appointment a WHERE " +
           "(a.studentId = :userId OR a.counsellorId = :userId) " +
           "AND a.scheduledStartTime > :now " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS') " +
           "ORDER BY a.scheduledStartTime ASC")
    List<Appointment> findUpcomingAppointments(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now);

    /**
     * Find upcoming appointments for a student.
     * 
     * @param studentId the student ID
     * @param now the current time
     * @return list of upcoming appointments for the student
     */
    @Query("SELECT a FROM Appointment a WHERE a.studentId = :studentId " +
           "AND a.scheduledStartTime > :now " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS') " +
           "ORDER BY a.scheduledStartTime ASC")
    List<Appointment> findUpcomingStudentAppointments(
            @Param("studentId") UUID studentId,
            @Param("now") LocalDateTime now);

    /**
     * Find upcoming appointments for a counsellor.
     * 
     * @param counsellorId the counsellor ID
     * @param now the current time
     * @return list of upcoming appointments for the counsellor
     */
    @Query("SELECT a FROM Appointment a WHERE a.counsellorId = :counsellorId " +
           "AND a.scheduledStartTime > :now " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS') " +
           "ORDER BY a.scheduledStartTime ASC")
    List<Appointment> findUpcomingCounsellorAppointments(
            @Param("counsellorId") UUID counsellorId,
            @Param("now") LocalDateTime now);

    /**
     * Find completed appointments for a student.
     * 
     * @param studentId the student ID
     * @return list of completed appointments for the student
     */
    @Query("SELECT a FROM Appointment a WHERE a.studentId = :studentId " +
           "AND a.status = 'COMPLETED' " +
           "ORDER BY a.completedAt DESC")
    List<Appointment> findCompletedStudentAppointments(@Param("studentId") UUID studentId);

    /**
     * Find completed appointments for a counsellor.
     * 
     * @param counsellorId the counsellor ID
     * @return list of completed appointments for the counsellor
     */
    @Query("SELECT a FROM Appointment a WHERE a.counsellorId = :counsellorId " +
           "AND a.status = 'COMPLETED' " +
           "ORDER BY a.completedAt DESC")
    List<Appointment> findCompletedCounsellorAppointments(@Param("counsellorId") UUID counsellorId);

    /**
     * Find cancelled appointments for a student.
     * 
     * @param studentId the student ID
     * @return list of cancelled appointments for the student
     */
    @Query("SELECT a FROM Appointment a WHERE a.studentId = :studentId " +
           "AND a.status = 'CANCELLED' " +
           "ORDER BY a.updatedAt DESC")
    List<Appointment> findCancelledStudentAppointments(@Param("studentId") UUID studentId);

    /**
     * Find cancelled appointments for a counsellor.
     * 
     * @param counsellorId the counsellor ID
     * @return list of cancelled appointments for the counsellor
     */
    @Query("SELECT a FROM Appointment a WHERE a.counsellorId = :counsellorId " +
           "AND a.status = 'CANCELLED' " +
           "ORDER BY a.updatedAt DESC")
    List<Appointment> findCancelledCounsellorAppointments(@Param("counsellorId") UUID counsellorId);

    /**
     * Check if there are any conflicting appointments for a counsellor.
     * Checks for overlapping time slots with SCHEDULED, CONFIRMED, or IN_PROGRESS status.
     * 
     * @param counsellorId the counsellor ID
     * @param startTime the start time of the new appointment
     * @param endTime the end time of the new appointment
     * @return true if there is a conflict
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE " +
           "a.counsellorId = :counsellorId " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS') " +
           "AND ((a.scheduledStartTime < :endTime AND a.scheduledEndTime > :startTime))")
    boolean hasConflictingAppointment(
            @Param("counsellorId") UUID counsellorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Check if there are any conflicting appointments for a counsellor, excluding a specific appointment.
     * 
     * @param counsellorId the counsellor ID
     * @param startTime the start time of the appointment
     * @param endTime the end time of the appointment
     * @param appointmentId the appointment ID to exclude
     * @return true if there is a conflict
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE " +
           "a.counsellorId = :counsellorId " +
           "AND a.id != :appointmentId " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS') " +
           "AND ((a.scheduledStartTime < :endTime AND a.scheduledEndTime > :startTime))")
    boolean hasConflictingAppointmentExcluding(
            @Param("counsellorId") UUID counsellorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("appointmentId") UUID appointmentId);

    /**
     * Find appointments for a counsellor on a specific date.
     * 
     * @param counsellorId the counsellor ID
     * @param startOfDay the start of the day
     * @param endOfDay the end of the day
     * @return list of appointments for the counsellor on that day
     */
    @Query("SELECT a FROM Appointment a WHERE a.counsellorId = :counsellorId " +
           "AND a.scheduledStartTime >= :startOfDay " +
           "AND a.scheduledStartTime < :endOfDay " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED') " +
           "ORDER BY a.scheduledStartTime ASC")
    List<Appointment> findCounsellorAppointmentsByDate(
            @Param("counsellorId") UUID counsellorId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Find appointments for a student on a specific date.
     * 
     * @param studentId the student ID
     * @param startOfDay the start of the day
     * @param endOfDay the end of the day
     * @return list of appointments for the student on that day
     */
    @Query("SELECT a FROM Appointment a WHERE a.studentId = :studentId " +
           "AND a.scheduledStartTime >= :startOfDay " +
           "AND a.scheduledStartTime < :endOfDay " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED') " +
           "ORDER BY a.scheduledStartTime ASC")
    List<Appointment> findStudentAppointmentsByDate(
            @Param("studentId") UUID studentId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Count appointments for a counsellor with a specific status.
     * 
     * @param counsellorId the counsellor ID
     * @param status the appointment status
     * @return count of appointments
     */
    long countByCounsellorIdAndStatus(UUID counsellorId, AppointmentStatus status);

    /**
     * Count appointments for a student with a specific status.
     * 
     * @param studentId the student ID
     * @param status the appointment status
     * @return count of appointments
     */
    long countByStudentIdAndStatus(UUID studentId, AppointmentStatus status);

    /**
     * Count appointments between a counsellor and student.
     * 
     * Used to verify if a counsellor has access to a student's data.
     * 
     * @param counsellorId the counsellor ID
     * @param studentId the student ID
     * @return count of appointments between the counsellor and student
     */
    long countByCounsellorIdAndStudentId(UUID counsellorId, UUID studentId);

    /**
     * Find the most recent appointment between a student and counsellor.
     * 
     * @param studentId the student ID
     * @param counsellorId the counsellor ID
     * @return Optional containing the most recent appointment if found
     */
    @Query("SELECT a FROM Appointment a WHERE a.studentId = :studentId " +
           "AND a.counsellorId = :counsellorId " +
           "ORDER BY a.scheduledStartTime DESC LIMIT 1")
    Optional<Appointment> findMostRecentAppointment(
            @Param("studentId") UUID studentId,
            @Param("counsellorId") UUID counsellorId);

    /**
     * Find appointments that need reminders (24 hours before scheduled time).
     * 
     * @param startTime the start of the reminder window
     * @param endTime the end of the reminder window
     * @return list of appointments needing reminders
     */
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.scheduledStartTime BETWEEN :startTime AND :endTime " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY a.scheduledStartTime ASC")
    List<Appointment> findAppointmentsNeedingReminders(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find appointments for reminder sending (24 hours before scheduled time).
     * 
     * @param now the current time
     * @param reminderTime the time 24 hours from now
     * @param reminderTimeEnd the end of the reminder window (5 minutes after reminderTime)
     * @return list of appointments needing reminders
     */
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.scheduledStartTime > :now " +
           "AND a.scheduledStartTime BETWEEN :reminderTime AND :reminderTimeEnd " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY a.scheduledStartTime ASC")
    List<Appointment> findAppointmentsForReminder(
            @Param("now") LocalDateTime now,
            @Param("reminderTime") LocalDateTime reminderTime,
            @Param("reminderTimeEnd") LocalDateTime reminderTimeEnd);
}
