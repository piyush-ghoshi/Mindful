package com.mindful.wellness.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MoodEntry entity representing a mood journal entry for a student.
 * 
 * Stores daily mood check-ins with emotional state, energy level, sleep quality,
 * and optional journal text. Includes sentiment analysis score and categorization.
 * 
 * Validates: Requirements 4, 15
 */
@Entity
@Table(name = "mood_entries", indexes = {
        @Index(name = "idx_student_id", columnList = "student_id"),
        @Index(name = "idx_recorded_at", columnList = "recorded_at"),
        @Index(name = "idx_mood_rating", columnList = "mood_rating")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Student ID is required")
    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @NotNull(message = "Mood rating is required")
    @Min(value = 1, message = "Mood rating must be at least 1")
    @Max(value = 5, message = "Mood rating must be at most 5")
    @Column(name = "mood_rating", nullable = false)
    private Integer moodRating;

    @Column(name = "energy_level")
    @Min(value = 1, message = "Energy level must be at least 1")
    @Max(value = 5, message = "Energy level must be at most 5")
    private Integer energyLevel;

    @Column(name = "sleep_quality")
    @Min(value = 1, message = "Sleep quality must be at least 1")
    @Max(value = 5, message = "Sleep quality must be at most 5")
    private Integer sleepQuality;

    @Column(name = "journal_text", columnDefinition = "TEXT")
    @Size(max = 5000, message = "Journal text cannot exceed 5000 characters")
    private String journalText;

    @Column(name = "sentiment_score")
    @DecimalMin(value = "-1.0", message = "Sentiment score must be at least -1.0")
    @DecimalMax(value = "1.0", message = "Sentiment score must be at most 1.0")
    private Double sentimentScore;

    @Column(name = "triggers", columnDefinition = "TEXT")
    private String triggers; // JSON array of trigger strings

    @Column(name = "activities", columnDefinition = "TEXT")
    private String activities; // JSON array of activity strings

    @Column(name = "emotions", columnDefinition = "TEXT")
    private String emotions; // JSON array of emotion strings

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;

    @NotNull(message = "Recorded timestamp is required")
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

