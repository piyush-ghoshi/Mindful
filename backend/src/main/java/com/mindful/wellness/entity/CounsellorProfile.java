package com.mindful.wellness.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CounsellorProfile entity representing a counsellor's profile information.
 * 
 * Contains counsellor-specific data including credentials, specializations,
 * availability schedule, and performance metrics.
 * 
 * Validates: Requirements 1, 2, 3, 25, 26
 */
@Entity
@Table(name = "counsellor_profiles", indexes = {
        @Index(name = "idx_counsellor_profiles_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_counsellor_profiles_license", columnList = "license_number", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounsellorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @NotNull(message = "Counsellor ID is required")
    @Column(name = "counsellor_id", nullable = false, length = 50)
    private String counsellorId;

    @Column(name = "institution_id")
    private UUID institutionId;

    @NotNull(message = "License number is required")
    @Column(name = "license_number", nullable = false, unique = true, length = 100)
    private String licenseNumber;

    @Column(name = "specializations", columnDefinition = "TEXT")
    private String specializations; // JSON array stored as string

    @Column(name = "qualifications", columnDefinition = "TEXT")
    private String qualifications; // JSON array stored as string

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "availability_schedule", columnDefinition = "TEXT")
    private String availabilitySchedule; // JSON stored as string

    @Column(name = "max_appointments_per_day", nullable = false)
    private Integer maxAppointmentsPerDay = 8;

    @Column(name = "appointment_duration", nullable = false)
    private Integer appointmentDuration = 30; // in minutes

    @Column(name = "rating", nullable = false)
    private Double rating = 0.0;

    @Column(name = "total_appointments", nullable = false)
    private Integer totalAppointments = 0;

    @Column(name = "is_accepting_new_students", nullable = false)
    private Boolean isAcceptingNewStudents = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
