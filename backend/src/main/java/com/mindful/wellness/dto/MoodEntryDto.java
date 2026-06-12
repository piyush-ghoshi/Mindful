package com.mindful.wellness.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for mood entry information.
 * 
 * Contains mood entry details for API responses.
 * 
 * Validates: Requirement 4, 15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodEntryDto {

    private UUID id;

    private UUID studentId;

    private Integer moodRating;

    private Integer energyLevel;

    private Integer sleepQuality;

    private List<String> emotions;

    private String journalText;

    private Double sentimentScore;

    private List<String> triggers;

    private List<String> activities;

    private Boolean isPrivate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recordedDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
