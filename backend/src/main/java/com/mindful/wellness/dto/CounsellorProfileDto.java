package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for counsellor profile information.
 * 
 * Validates: Requirements 1, 2, 3, 6, 12, 13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounsellorProfileDto {

    private UUID id;

    private UUID userId;

    private String counsellorId;

    private UUID institutionId;

    private String licenseNumber;

    private List<String> specializations;

    private List<String> qualifications;

    private Integer yearsOfExperience;

    private String bio;

    private AvailabilityScheduleDto availabilitySchedule;

    private Integer maxAppointmentsPerDay;

    private Integer appointmentDuration;

    private Double rating;

    private Integer totalAppointments;

    private Boolean isAcceptingNewStudents;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;
}
