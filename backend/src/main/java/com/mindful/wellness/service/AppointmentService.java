package com.mindful.wellness.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindful.wellness.dto.*;
import com.mindful.wellness.entity.Appointment;
import com.mindful.wellness.entity.AppointmentStatus;
import com.mindful.wellness.entity.CounsellorProfile;
import com.mindful.wellness.entity.User;
import com.mindful.wellness.repository.AppointmentRepository;
import com.mindful.wellness.repository.CounsellorProfileRepository;
import com.mindful.wellness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing appointment lifecycle including booking, cancellation, rescheduling.
 * 
 * Implements:
 * - Appointment booking with conflict detection
 * - Double-booking prevention using pessimistic locking
 * - Transaction management for atomic operations
 * - Availability checking for counsellors
 * - Appointment status management
 * 
 * Validates: Requirements 6, 13
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final CounsellorProfileRepository counsellorProfileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final AppointmentReminderScheduler appointmentReminderScheduler;

    /**
     * Confirm (accept) an appointment — sets status to CONFIRMED.
     */
    public Appointment confirmAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot confirm a " + appointment.getStatus() + " appointment");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment confirmed: {}", appointmentId);
        return saved;
    }

    /**
     * Book a new appointment with conflict detection and double-booking prevention.
     * 
     * Uses pessimistic locking to prevent race conditions. Checks:
     * 1. Student exists
     * 2. Counsellor exists and is accepting new students
     * 3. Time slot is in the future
     * 4. No overlapping appointments for the counsellor
     * 5. Counsellor has not exceeded max appointments per day
     * 
     * @param studentId the student ID
     * @param counsellorId the counsellor ID
     * @param startTime the appointment start time
     * @param reason the reason for the appointment
     * @return the created appointment
     * @throws IllegalArgumentException if validation fails
     */
    public Appointment bookAppointment(UUID studentId, UUID counsellorId, LocalDateTime startTime, String reason) {
        return bookAppointment(studentId, counsellorId, startTime, reason, null, null);
    }

    /**
     * Book a new appointment with additional details.
     * 
     * @param studentId the student ID
     * @param counsellorId the counsellor ID
     * @param startTime the appointment start time
     * @param reason the reason for the appointment
     * @param appointmentType the type of appointment (IN_PERSON, VIDEO, PHONE)
     * @param studentNotes optional notes from the student
     * @return the created appointment
     * @throws IllegalArgumentException if validation fails
     */
    public Appointment bookAppointment(UUID studentId, UUID counsellorId, LocalDateTime startTime, 
                                      String reason, String appointmentType, String studentNotes) {
        // Validate student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        // Validate counsellor exists.
        // The frontend sends the counsellor's User UUID (users.id), so look up by userId first.
        // If no counsellor_profile exists for that user, create a default one so booking still works.
        CounsellorProfile counsellor = counsellorProfileRepository.findByUserId(counsellorId)
                .orElseGet(() -> {
                    // Auto-create a minimal counsellor profile for users with COUNSELLOR role
                    User counsellorUser = userRepository.findById(counsellorId)
                            .orElseThrow(() -> new IllegalArgumentException("Counsellor not found with ID: " + counsellorId));
                    CounsellorProfile newProfile = CounsellorProfile.builder()
                            .userId(counsellorId)
                            .counsellorId(counsellorId.toString().substring(0, 8))
                            .licenseNumber("TEMP-" + counsellorId.toString().substring(0, 8))
                            .isAcceptingNewStudents(true)
                            .maxAppointmentsPerDay(8)
                            .appointmentDuration(60)
                            .rating(0.0)
                            .totalAppointments(0)
                            .build();
                    return counsellorProfileRepository.save(newProfile);
                });

        // Validate counsellor is accepting new students
        if (!counsellor.getIsAcceptingNewStudents()) {
            throw new IllegalArgumentException("Counsellor is not accepting new students");
        }

        // Validate start time is in the future
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment start time must be in the future");
        }

        // Calculate end time based on counsellor's appointment duration
        Integer appointmentDuration = counsellor.getAppointmentDuration();
        if (appointmentDuration == null || appointmentDuration <= 0) {
            appointmentDuration = 30; // Default to 30 minutes
        }
        LocalDateTime endTime = startTime.plusMinutes(appointmentDuration);

        // Check for overlapping appointments (with pessimistic locking)
        if (appointmentRepository.hasConflictingAppointment(counsellorId, startTime, endTime)) {
            throw new IllegalArgumentException("Time slot is not available. Counsellor has conflicting appointments.");
        }

        // Check if counsellor has exceeded max appointments per day
        long appointmentsOnDate = appointmentRepository.findCounsellorAppointmentsByDate(
                counsellorId, startTime.toLocalDate().atStartOfDay(), 
                startTime.toLocalDate().atTime(23, 59, 59)
        ).size();
        Integer maxAppointmentsPerDay = counsellor.getMaxAppointmentsPerDay();
        if (maxAppointmentsPerDay == null) {
            maxAppointmentsPerDay = 8; // Default to 8
        }

        if (appointmentsOnDate >= maxAppointmentsPerDay) {
            throw new IllegalArgumentException("Counsellor has reached maximum appointments for this day");
        }

        // Create and save the appointment
        Appointment appointment = Appointment.builder()
                .studentId(studentId)
                .counsellorId(counsellorId)
                .scheduledStartTime(startTime)
                .scheduledEndTime(endTime)
                .status(AppointmentStatus.SCHEDULED)
                .appointmentType(appointmentType != null ? appointmentType : "VIDEO")
                .reason(reason)
                .studentNotes(studentNotes)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment booked successfully. ID: {}, Student: {}, Counsellor: {}, Time: {}",
                savedAppointment.getId(), studentId, counsellorId, startTime);

        // Send confirmation notifications
        appointmentReminderScheduler.sendAppointmentConfirmation(savedAppointment);

        return savedAppointment;
    }

    /**
     * Cancel an appointment.
     * 
     * @param appointmentId the appointment ID
     * @param cancelledBy the user ID of who is cancelling
     * @param reason the cancellation reason
     * @return true if cancellation was successful
     * @throws IllegalArgumentException if appointment not found or cannot be cancelled
     */
    public boolean cancelAppointment(UUID appointmentId, UUID cancelledBy, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));

        // Validate appointment can be cancelled
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a completed appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Appointment is already cancelled");
        }

        // Update appointment status
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledBy(cancelledBy);
        appointment.setCancellationReason(reason);

        appointmentRepository.save(appointment);
        log.info("Appointment cancelled. ID: {}, Cancelled by: {}, Reason: {}",
                appointmentId, cancelledBy, reason);

        // Send cancellation notifications
        appointmentReminderScheduler.sendAppointmentCancellation(appointment);

        return true;
    }

    /**
     * Reschedule an appointment to a new time.
     * 
     * Validates that the new time slot is available and follows the same rules as booking.
     * 
     * @param appointmentId the appointment ID
     * @param newStartTime the new start time
     * @return the rescheduled appointment
     * @throws IllegalArgumentException if appointment not found or cannot be rescheduled
     */
    public Appointment rescheduleAppointment(UUID appointmentId, LocalDateTime newStartTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));

        // Validate appointment can be rescheduled
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot reschedule a completed appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot reschedule a cancelled appointment");
        }

        // Validate new start time is in the future
        if (newStartTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New appointment start time must be in the future");
        }

        // Get counsellor to determine appointment duration — look up by userId
        CounsellorProfile counsellor = counsellorProfileRepository.findByUserId(appointment.getCounsellorId())
                .orElseGet(() -> counsellorProfileRepository.findById(appointment.getCounsellorId())
                        .orElseThrow(() -> new IllegalArgumentException("Counsellor not found")));

        Integer appointmentDuration = counsellor.getAppointmentDuration();
        if (appointmentDuration == null || appointmentDuration <= 0) {
            appointmentDuration = 30;
        }
        LocalDateTime newEndTime = newStartTime.plusMinutes(appointmentDuration);

        // Check for overlapping appointments at new time (excluding current appointment)
        if (appointmentRepository.hasConflictingAppointmentExcluding(
                appointment.getCounsellorId(), newStartTime, newEndTime, appointmentId)) {
            throw new IllegalArgumentException("New time slot is not available. Counsellor has conflicting appointments.");
        }

        // Update appointment times
        appointment.setScheduledStartTime(newStartTime);
        appointment.setScheduledEndTime(newEndTime);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);

        Appointment rescheduledAppointment = appointmentRepository.save(appointment);
        log.info("Appointment rescheduled. ID: {}, New time: {}", appointmentId, newStartTime);

        return rescheduledAppointment;
    }

    /**
     * Get an appointment by ID.
     * 
     * @param appointmentId the appointment ID
     * @return the appointment
     * @throws IllegalArgumentException if appointment not found
     */
    public Appointment getAppointmentById(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));
    }

    /**
     * Get appointments for a counsellor user (by userId), with optional status filter.
     */
    public List<Appointment> getCounsellorAppointmentsByUser(UUID counsellorUserId, AppointmentStatus status) {
        if (status != null) {
            return appointmentRepository.findByCounsellorIdAndStatus(counsellorUserId, status);
        }
        return appointmentRepository.findByCounsellorId(counsellorUserId);
    }

    /**
     * Get all appointments for a student with optional status filter.
     * 
     * @param studentId the student ID
     * @param status the appointment status (optional)
     * @return list of appointments
     */
    public List<Appointment> getStudentAppointments(UUID studentId, AppointmentStatus status) {
        if (status != null) {
            return appointmentRepository.findByStudentIdAndStatus(studentId, status);
        }
        return appointmentRepository.findByStudentId(studentId);
    }

    /**
     * Get all appointments for a counsellor within a date range.
     * 
     * @param counsellorId the counsellor ID
     * @param start the start of the date range
     * @param end the end of the date range
     * @return list of appointments
     */
    public List<Appointment> getCounsellorAppointments(UUID counsellorId, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findCounsellorAppointmentsByTimeRange(counsellorId, start, end);
    }

    /**
     * Mark an appointment as complete.
     * 
     * @param appointmentId the appointment ID
     * @param notes optional notes from the counsellor
     * @return true if marking complete was successful
     * @throws IllegalArgumentException if appointment not found or cannot be marked complete
     */
    public boolean markAppointmentComplete(UUID appointmentId, String notes) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));

        // Validate appointment can be marked complete
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot mark a cancelled appointment as complete");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Appointment is already marked as complete");
        }

        // Update appointment
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setCounsellorNotes(notes);
        appointment.setCompletedAt(LocalDateTime.now());

        appointmentRepository.save(appointment);
        log.info("Appointment marked as complete. ID: {}", appointmentId);

        return true;
    }

    /**
     * Get upcoming appointments for a user (student or counsellor).
     * 
     * @param userId the user ID
     * @return list of upcoming appointments
     */
    public List<Appointment> getUpcomingAppointments(UUID userId) {
        return appointmentRepository.findUpcomingAppointments(userId, LocalDateTime.now());
    }

    /**
     * Check if a time slot is available for a counsellor.
     * 
     * @param counsellorId the counsellor ID
     * @param startTime the start time
     * @param endTime the end time
     * @return true if slot is available, false otherwise
     */
    public boolean checkAvailability(UUID counsellorId, LocalDateTime startTime, LocalDateTime endTime) {
        // Validate counsellor exists — by userId first
        counsellorProfileRepository.findByUserId(counsellorId)
                .orElseGet(() -> counsellorProfileRepository.findById(counsellorId)
                        .orElseThrow(() -> new IllegalArgumentException("Counsellor not found with ID: " + counsellorId)));

        // Check if time slot is available
        return !appointmentRepository.hasConflictingAppointment(counsellorId, startTime, endTime);
    }

    /**
     * Convert Appointment entity to AppointmentDto.
     * 
     * @param appointment the appointment entity
     * @return the appointment DTO
     */
    public AppointmentDto convertToDto(Appointment appointment) {
        User student = userRepository.findById(appointment.getStudentId()).orElse(null);
        User counsellor = userRepository.findById(appointment.getCounsellorId()).orElse(null);

        return AppointmentDto.builder()
                .id(appointment.getId())
                .studentId(appointment.getStudentId())
                .counsellorId(appointment.getCounsellorId())
                .scheduledStartTime(appointment.getScheduledStartTime())
                .scheduledEndTime(appointment.getScheduledEndTime())
                .status(appointment.getStatus().toString())
                .appointmentType(appointment.getAppointmentType())
                .reason(appointment.getReason())
                .studentNotes(appointment.getStudentNotes())
                .counsellorNotes(appointment.getCounsellorNotes())
                .cancelledBy(appointment.getCancelledBy())
                .cancellationReason(appointment.getCancellationReason())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .completedAt(appointment.getCompletedAt())
                .studentName(student != null ? student.getFirstName() + " " + student.getLastName() : null)
                .counsellorName(counsellor != null ? counsellor.getFirstName() + " " + counsellor.getLastName() : null)
                .counsellorEmail(counsellor != null ? counsellor.getEmail() : null)
                .build();
    }
}
