package com.mindful.wellness.dto;

import com.mindful.wellness.entity.AnalysisMethod;
import com.mindful.wellness.entity.MoodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for MoodAssessment entity.
 * Used for API responses and service layer communication.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodAssessmentDto {
    
    private UUID id;
    private UUID sessionId;
    private UUID messageId;
    private UUID userId;
    
    // Mood Analysis
    private MoodType detectedMood;
    private BigDecimal moodIntensity;
    private BigDecimal sentimentScore;
    private List<String> dominantEmotions;
    
    // Analysis Metadata
    private AnalysisMethod analysisMethod;
    private BigDecimal confidenceScore;
    private List<String> riskIndicators;
    
    private LocalDateTime createdAt;
    
    // Computed fields
    private String moodDescription;
    private boolean isConcerning;
    private boolean isHighIntensity;
    private boolean isNegativeSentiment;
}
