package com.mindful.wellness.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for appointment information.
 * 
 * Contains complete appointment details for API responses.
 * 
 * Validates: Requirement 6, 13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDto {

    private UUID id;

    private UUID studentId;

    private UUID counsellorId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledEndTime;

    private String status;

    private String appointmentType;

    private String reason;

    private String studentNotes;

    private String counsellorNotes;

    private UUID cancelledBy;

    private String cancellationReason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    // Additional fields for convenience
    private String studentName;

    private String counsellorName;

    private String counsellorEmail;
}
