package com.mindful.wellness.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MentalHealthReportDto {
    private UUID id;
    private UUID userId;
    private UUID sessionId;
    private Integer phaseNumber;
    private String title;
    private String mentalStateLevel;     // LOW | MODERATE | HIGH | SEVERE
    private Integer wellnessScore;       // 0-100
    private List<String> conditionPoints;
    private List<String> recommendedExercises;
    private List<String> recommendedMeditations;
    private String conclusion;
    private Boolean counsellorReferralSuggested;
    private LocalDateTime createdAt;

    // Derived for display
    private String userName;
    private String userEmail;
}
