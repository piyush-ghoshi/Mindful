package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for cancelling an appointment request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelAppointmentRequest {

    private String reason; // optional
}
