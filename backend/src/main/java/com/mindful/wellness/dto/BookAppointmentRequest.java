package com.mindful.wellness.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for booking an appointment request.
 * 
 * Contains the necessary information to book a new appointment:
 * - counsellorId: The ID of the counsellor
 * - startTime: The desired start time for the appointment
 * - reason: The reason for the appointment
 * - appointmentType: Type of appointment (IN_PERSON, VIDEO, PHONE)
 * 
 * Validates: Requirement 6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookAppointmentRequest {

    @NotNull(message = "Counsellor ID is required")
    private UUID counsellorId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    private String reason; // optional

    private String appointmentType; // IN_PERSON, VIDEO, PHONE

    private String studentNotes;
}
