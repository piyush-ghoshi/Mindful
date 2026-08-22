package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a significant mood shift detected in conversation.
 * Used to identify concerning changes or improvements.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodShiftDto {
    
    private LocalDateTime fromTime;
    private LocalDateTime toTime;
    
    private String fromMood;
    private String toMood;
    
    private BigDecimal fromSentiment;
    private BigDecimal toSentiment;
    
    private BigDecimal sentimentChange;
    private String shiftType; // POSITIVE, NEGATIVE, NEUTRAL
    private String severity; // MINOR, MODERATE, MAJOR
    
    private String description;
    private boolean isConcerning;
}
