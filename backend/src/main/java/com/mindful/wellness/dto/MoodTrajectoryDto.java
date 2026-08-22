package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing mood trajectory over time for a session or user.
 * Used for visualizing mood changes and detecting patterns.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodTrajectoryDto {
    
    private UUID sessionId;
    private UUID userId;
    
    // Trajectory data points (time-series)
    private List<MoodDataPoint> dataPoints;
    
    // Summary statistics
    private BigDecimal averageSentiment;
    private BigDecimal averageIntensity;
    private String dominantMood;
    private String trend; // IMPROVING, DECLINING, STABLE
    
    // Insights
    private List<String> insights;
    private boolean hasConcerningPattern;
    private List<MoodShiftDto> significantShifts;
    
    /**
     * Single data point in the mood trajectory.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MoodDataPoint {
        private LocalDateTime timestamp;
        private String mood;
        private BigDecimal intensity;
        private BigDecimal sentiment;
        private int messageIndex;
    }
}
