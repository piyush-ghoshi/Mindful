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
 * AvailabilityException entity representing exceptions to a counsellor's regular availability.
 * 
 * Contains date-specific exceptions such as holidays, time-off, or custom availability slots.
 * Supports custom time slots for specific dates and stores exception metadata as JSONB.
 * 
 * Validates: Requirement 13
 */
@Entity
@Table(name = "availability_exceptions", indexes = {
        @Index(name = "idx_availability_exceptions_counsellor_id", columnList = "counsellor_id"),
        @Index(name = "idx_availability_exceptions_counsellor_date", columnList = "counsellor_id, exception_date", unique = true),
        @Index(name = "idx_availability_exceptions_date_range", columnList = "counsellor_id, exception_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityException {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Counsellor ID is required")
    @Column(name = "counsellor_id", nullable = false)
    private UUID counsellorId;

    @NotNull(message = "Exception date is required")
    @Column(name = "exception_date", nullable = false)
    private LocalDate exceptionDate;

    @NotNull(message = "Availability flag is required")
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = false;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "custom_slots", columnDefinition = "TEXT")
    private String customSlots; // JSON array of TimeSlot objects stored as string

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
