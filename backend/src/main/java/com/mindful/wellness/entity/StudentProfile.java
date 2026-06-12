package com.mindful.wellness.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StudentProfile entity representing a student's profile information.
 * 
 * Contains student-specific data including personal information, emergency contacts,
 * wellness preferences, and privacy settings.
 * 
 * Validates: Requirements 1, 2, 3, 25, 26
 */
@Entity
@Table(name = "student_profiles", indexes = {
        @Index(name = "idx_student_profiles_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_student_profiles_student_id", columnList = "student_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @NotNull(message = "Student ID is required")
    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "institution_id")
    private UUID institutionId;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 50)
    private String gender;

    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "consent_for_data_sharing", nullable = false)
    private Boolean consentForDataSharing = false;

    @Column(name = "consent_for_anonymous_analytics", nullable = false)
    private Boolean consentForAnonymousAnalytics = false;

    @Column(name = "preferred_counsellor_gender", length = 50)
    private String preferredCounsellorGender;

    @Column(name = "wellness_goals", columnDefinition = "TEXT")
    private String wellnessGoals; // JSON array stored as string

    @Column(name = "notification_preferences", columnDefinition = "TEXT")
    private String notificationPreferences; // JSON stored as string

    @Column(name = "privacy_settings", columnDefinition = "TEXT")
    private String privacySettings; // JSON stored as string

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
