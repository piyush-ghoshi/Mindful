package com.mindful.wellness.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for rescheduling an appointment request.
 * 
 * Contains the new start time for the appointment.
 * 
 * Validates: Requirement 6, 13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleAppointmentRequest {

    @NotNull(message = "New start time is required")
    private LocalDateTime newStartTime;
}
