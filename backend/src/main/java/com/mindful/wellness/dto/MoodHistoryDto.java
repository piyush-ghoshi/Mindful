package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for mood history with pagination and statistics.
 * 
 * Contains paginated mood entries, average mood, mood distribution,
 * and trend analysis.
 * 
 * Validates: Requirement 4, 15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodHistoryDto {

    /**
     * List of mood entries for the requested period
     */
    private List<MoodEntryDto> entries;

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
     * Pagination information
     */
    private PaginationInfo pagination;

    /**
     * Inner class for pagination information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer pageSize;
        private Long totalElements;
        private Integer totalPages;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
}
