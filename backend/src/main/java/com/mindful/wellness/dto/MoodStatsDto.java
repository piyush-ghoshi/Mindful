package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for mood statistics and trends.
 * 
 * Contains aggregated mood statistics, distribution, and trend analysis
 * without pagination.
 * 
 * Validates: Requirement 4, 15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodStatsDto {

    /**
     * Average mood rating for the period (1-5 scale)
     */
    private Double averageMoodRating;

    /**
     * Average energy level for the period (1-5 scale)
     */
    private Double averageEnergyLevel;

    /**
     * Average sleep quality for the period (1-5 scale)
     */
    private Double averageSleepQuality;

    /**
     * Mood distribution - count of entries by mood rating
     * Key: mood rating (1-5), Value: count
     */
    private Map<Integer, Long> moodDistribution;

    /**
     * Emotion frequency - count of entries by emotion
     * Key: emotion name, Value: count
     */
    private Map<String, Long> emotionFrequency;

    /**
     * Trend direction: IMPROVING, DECLINING, or STABLE
     */
    private String trendDirection;

    /**
     * Trend percentage change from start to end of period
     */
    private Double trendPercentageChange;

    /**
     * Mood variability (standard deviation)
     */
    private Double moodVariability;

    /**
     * List of concerning patterns detected
     */
    private List<String> concerningPatterns;

    /**
     * List of recommendations based on mood data
     */
    private List<String> recommendations;

    /**
     * Total number of entries in the period
     */
    private Long totalEntries;

    /**
     * Period covered by the statistics (e.g., "Last 30 days")
     */
    private String period;

    /**
     * Most common emotions in the period
     */
    private List<String> mostCommonEmotions;

    /**
     * Highest mood rating in the period
     */
    private Integer highestMoodRating;

    /**
     * Lowest mood rating in the period
     */
    private Integer lowestMoodRating;
}
