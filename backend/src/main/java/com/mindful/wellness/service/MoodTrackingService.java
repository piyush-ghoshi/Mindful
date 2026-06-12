package com.mindful.wellness.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindful.wellness.dto.MoodEntryDto;
import com.mindful.wellness.dto.MoodHistoryDto;
import com.mindful.wellness.dto.MoodStatsDto;
import com.mindful.wellness.entity.MoodEntry;
import com.mindful.wellness.repository.AppointmentRepository;
import com.mindful.wellness.repository.MoodEntryRepository;
import com.mindful.wellness.util.SentimentAnalysisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for mood tracking operations.
 * 
 * Handles mood entry recording, history retrieval, trend analysis,
 * statistics generation, and counsellor access control.
 * 
 * Validates: Requirements 4, 15
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MoodTrackingService {

    private final MoodEntryRepository moodEntryRepository;
    private final AppointmentRepository appointmentRepository;
    private final SentimentAnalysisUtil sentimentAnalysisUtil;
    private final ObjectMapper objectMapper;

    /**
     * Record a new mood entry for a student.
     * 
     * Validates input, performs sentiment analysis on journal text,
     * and saves the entry to the database.
     * 
     * @param studentId the student ID
     * @param moodEntry the mood entry to record
     * @return the recorded mood entry DTO
     * @throws IllegalArgumentException if input is invalid
     */
    public MoodEntryDto recordMoodEntry(UUID studentId, MoodEntry moodEntry) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        if (moodEntry.getMoodRating() == null || moodEntry.getMoodRating() < 1 || moodEntry.getMoodRating() > 5) {
            throw new IllegalArgumentException("Mood rating must be between 1 and 5");
        }

        // Validate energy level if provided
        if (moodEntry.getEnergyLevel() != null && 
            (moodEntry.getEnergyLevel() < 1 || moodEntry.getEnergyLevel() > 5)) {
            throw new IllegalArgumentException("Energy level must be between 1 and 5");
        }

        // Validate sleep quality if provided
        if (moodEntry.getSleepQuality() != null && 
            (moodEntry.getSleepQuality() < 1 || moodEntry.getSleepQuality() > 5)) {
            throw new IllegalArgumentException("Sleep quality must be between 1 and 5");
        }

        // Validate journal text length
        if (moodEntry.getJournalText() != null && moodEntry.getJournalText().length() > 5000) {
            throw new IllegalArgumentException("Journal text cannot exceed 5000 characters");
        }

        moodEntry.setStudentId(studentId);
        if (moodEntry.getRecordedAt() == null) {
            moodEntry.setRecordedAt(LocalDateTime.now());
        }

        // Perform sentiment analysis on journal text
        if (moodEntry.getJournalText() != null && !moodEntry.getJournalText().isEmpty()) {
            Double sentimentScore = sentimentAnalysisUtil.analyzeSentiment(moodEntry.getJournalText());
            moodEntry.setSentimentScore(sentimentScore);
            log.debug("Sentiment analysis for student {}: {}", studentId, sentimentScore);
        }

        MoodEntry savedEntry = moodEntryRepository.save(moodEntry);
        log.info("Mood entry recorded for student {}: rating={}, timestamp={}", 
                 studentId, moodEntry.getMoodRating(), moodEntry.getRecordedAt());
        
        return convertToDto(savedEntry);
    }

    /**
     * Get mood history for a student with pagination.
     * 
     * @param studentId the student ID
     * @param days number of days to retrieve (default 30)
     * @param pageable pagination parameters
     * @return mood history with pagination
     * @throws IllegalArgumentException if student ID is null
     */
    @Transactional(readOnly = true)
    public MoodHistoryDto getMoodHistory(UUID studentId, Integer days, Pageable pageable) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        if (days == null || days <= 0) {
            days = 30;
        }

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        Page<MoodEntry> entries = moodEntryRepository.findByStudentIdAndRecordedAtBetweenOrderByRecordedAtDesc(
                studentId, startTime, endTime, pageable
        );

        List<MoodEntryDto> entryDtos = entries.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // Calculate statistics
        Double averageMoodRating = moodEntryRepository.getAverageMoodRating(studentId, startTime, endTime);
        Double averageEnergyLevel = moodEntryRepository.getAverageEnergyLevel(studentId, startTime, endTime);
        Double averageSleepQuality = moodEntryRepository.getAverageSleepQuality(studentId, startTime, endTime);

        // Get all entries for distribution calculation
        List<MoodEntry> allEntries = moodEntryRepository.findByStudentIdAndRecordedAtBetweenOrderByRecordedAtDesc(
                studentId, startTime, endTime
        );

        Map<Integer, Long> moodDistribution = calculateMoodDistribution(allEntries);
        Map<String, Long> emotionFrequency = calculateEmotionFrequency(allEntries);
        String trendDirection = calculateTrendDirection(allEntries);
        Double trendPercentageChange = calculateTrendPercentageChange(allEntries);
        List<String> concerningPatterns = detectConcerningPatterns(allEntries, averageMoodRating);
        List<String> recommendations = generateRecommendations(allEntries, averageMoodRating);

        MoodHistoryDto.PaginationInfo paginationInfo = MoodHistoryDto.PaginationInfo.builder()
                .currentPage(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(entries.getTotalElements())
                .totalPages(entries.getTotalPages())
                .hasNext(entries.hasNext())
                .hasPrevious(entries.hasPrevious())
                .build();

        return MoodHistoryDto.builder()
                .entries(entryDtos)
                .averageMoodRating(averageMoodRating)
                .averageEnergyLevel(averageEnergyLevel)
                .averageSleepQuality(averageSleepQuality)
                .moodDistribution(moodDistribution)
                .emotionFrequency(emotionFrequency)
                .trendDirection(trendDirection)
                .trendPercentageChange(trendPercentageChange)
                .concerningPatterns(concerningPatterns)
                .recommendations(recommendations)
                .totalEntries((long) allEntries.size())
                .pagination(paginationInfo)
                .build();
    }

    /**
     * Get mood statistics for a student.
     * 
     * @param studentId the student ID
     * @param days number of days to analyze (default 30)
     * @return mood statistics
     * @throws IllegalArgumentException if student ID is null
     */
    @Transactional(readOnly = true)
    public MoodStatsDto getMoodStats(UUID studentId, Integer days) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        if (days == null || days <= 0) {
            days = 30;
        }

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        // Calculate statistics
        Double averageMoodRating = moodEntryRepository.getAverageMoodRating(studentId, startTime, endTime);
        Double averageEnergyLevel = moodEntryRepository.getAverageEnergyLevel(studentId, startTime, endTime);
        Double averageSleepQuality = moodEntryRepository.getAverageSleepQuality(studentId, startTime, endTime);

        // Get all entries for distribution calculation
        List<MoodEntry> allEntries = moodEntryRepository.findByStudentIdAndRecordedAtBetweenOrderByRecordedAtDesc(
                studentId, startTime, endTime
        );

        Map<Integer, Long> moodDistribution = calculateMoodDistribution(allEntries);
        Map<String, Long> emotionFrequency = calculateEmotionFrequency(allEntries);
        String trendDirection = calculateTrendDirection(allEntries);
        Double trendPercentageChange = calculateTrendPercentageChange(allEntries);
        Double moodVariability = calculateMoodVariability(allEntries);
        List<String> concerningPatterns = detectConcerningPatterns(allEntries, averageMoodRating);
        List<String> recommendations = generateRecommendations(allEntries, averageMoodRating);
        List<String> mostCommonEmotions = getMostCommonEmotions(emotionFrequency, 3);
        Integer highestMoodRating = getHighestMoodRating(allEntries);
        Integer lowestMoodRating = getLowestMoodRating(allEntries);

        return MoodStatsDto.builder()
                .averageMoodRating(averageMoodRating)
                .averageEnergyLevel(averageEnergyLevel)
                .averageSleepQuality(averageSleepQuality)
                .moodDistribution(moodDistribution)
                .emotionFrequency(emotionFrequency)
                .trendDirection(trendDirection)
                .trendPercentageChange(trendPercentageChange)
                .moodVariability(moodVariability)
                .concerningPatterns(concerningPatterns)
                .recommendations(recommendations)
                .totalEntries((long) allEntries.size())
                .period("Last " + days + " days")
                .mostCommonEmotions(mostCommonEmotions)
                .highestMoodRating(highestMoodRating)
                .lowestMoodRating(lowestMoodRating)
                .build();
    }

    /**
     * Convert MoodEntry entity to DTO.
     * 
     * @param entry the mood entry entity
     * @return the mood entry DTO
     */
    private MoodEntryDto convertToDto(MoodEntry entry) {
        List<String> emotions = parseJsonArray(entry.getEmotions());
        List<String> triggers = parseJsonArray(entry.getTriggers());
        List<String> activities = parseJsonArray(entry.getActivities());

        return MoodEntryDto.builder()
                .id(entry.getId())
                .studentId(entry.getStudentId())
                .moodRating(entry.getMoodRating())
                .energyLevel(entry.getEnergyLevel())
                .sleepQuality(entry.getSleepQuality())
                .emotions(emotions)
                .journalText(entry.getJournalText())
                .sentimentScore(entry.getSentimentScore())
                .triggers(triggers)
                .activities(activities)
                .isPrivate(entry.getIsPrivate())
                .recordedDate(entry.getRecordedAt())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }

    /**
     * Parse JSON array string to list of strings.
     * 
     * @param jsonArray the JSON array string
     * @return list of strings or empty list if null/invalid
     */
    private List<String> parseJsonArray(String jsonArray) {
        if (jsonArray == null || jsonArray.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(jsonArray, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON array: {}", jsonArray, e);
            return new ArrayList<>();
        }
    }

    /**
     * Calculate mood distribution (count by rating).
     * 
     * @param entries the mood entries
     * @return map of mood rating to count
     */
    private Map<Integer, Long> calculateMoodDistribution(List<MoodEntry> entries) {
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        for (MoodEntry entry : entries) {
            if (entry.getMoodRating() != null) {
                distribution.put(entry.getMoodRating(),
                        distribution.get(entry.getMoodRating()) + 1);
            }
        }

        return distribution;
    }

    /**
     * Calculate emotion frequency.
     * 
     * @param entries the mood entries
     * @return map of emotion to count
     */
    private Map<String, Long> calculateEmotionFrequency(List<MoodEntry> entries) {
        Map<String, Long> frequency = new HashMap<>();

        for (MoodEntry entry : entries) {
            List<String> emotions = parseJsonArray(entry.getEmotions());
            for (String emotion : emotions) {
                frequency.put(emotion, frequency.getOrDefault(emotion, 0L) + 1);
            }
        }

        return frequency;
    }

    /**
     * Calculate trend direction based on mood ratings.
     * 
     * @param entries the mood entries (should be sorted by date descending)
     * @return IMPROVING, DECLINING, or STABLE
     */
    private String calculateTrendDirection(List<MoodEntry> entries) {
        if (entries.size() < 2) {
            return "STABLE";
        }

        // Split entries into two halves
        int midpoint = entries.size() / 2;
        List<MoodEntry> firstHalf = entries.subList(midpoint, entries.size());
        List<MoodEntry> secondHalf = entries.subList(0, midpoint);

        double firstHalfAvg = firstHalf.stream()
                .mapToInt(e -> e.getMoodRating() != null ? e.getMoodRating() : 0)
                .average()
                .orElse(0);

        double secondHalfAvg = secondHalf.stream()
                .mapToInt(e -> e.getMoodRating() != null ? e.getMoodRating() : 0)
                .average()
                .orElse(0);

        double difference = secondHalfAvg - firstHalfAvg;
        if (difference > 0.5) {
            return "IMPROVING";
        } else if (difference < -0.5) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }

    /**
     * Calculate trend percentage change.
     * 
     * @param entries the mood entries (should be sorted by date descending)
     * @return percentage change from start to end
     */
    private Double calculateTrendPercentageChange(List<MoodEntry> entries) {
        if (entries.size() < 2) {
            return 0.0;
        }

        // Most recent entry is first (descending order)
        Integer mostRecentRating = entries.get(0).getMoodRating();
        Integer oldestRating = entries.get(entries.size() - 1).getMoodRating();

        if (mostRecentRating == null || oldestRating == null || oldestRating == 0) {
            return 0.0;
        }

        return ((double) (mostRecentRating - oldestRating) / oldestRating) * 100;
    }

    /**
     * Calculate mood variability (standard deviation).
     * 
     * @param entries the mood entries
     * @return standard deviation of mood ratings
     */
    private Double calculateMoodVariability(List<MoodEntry> entries) {
        if (entries.isEmpty()) {
            return 0.0;
        }

        double average = entries.stream()
                .mapToInt(e -> e.getMoodRating() != null ? e.getMoodRating() : 0)
                .average()
                .orElse(0);

        double sumOfSquares = entries.stream()
                .mapToDouble(e -> {
                    int rating = e.getMoodRating() != null ? e.getMoodRating() : 0;
                    return Math.pow(rating - average, 2);
                })
                .sum();

        return Math.sqrt(sumOfSquares / entries.size());
    }

    /**
     * Detect concerning patterns in mood data.
     * 
     * @param entries the mood entries
     * @param averageMoodRating the average mood rating
     * @return list of concerning patterns
     */
    private List<String> detectConcerningPatterns(List<MoodEntry> entries, Double averageMoodRating) {
        List<String> patterns = new ArrayList<>();

        if (entries.isEmpty()) {
            return patterns;
        }

        // Check for consistently low mood
        if (averageMoodRating != null && averageMoodRating < 2.5) {
            patterns.add("Consistently low mood ratings detected");
        }

        // Check for declining trend
        String trend = calculateTrendDirection(entries);
        if ("DECLINING".equals(trend)) {
            patterns.add("Mood trend is declining");
        }

        // Check for high variability
        Double variability = calculateMoodVariability(entries);
        if (variability > 1.5) {
            patterns.add("High mood variability detected");
        }

        // Check for low energy levels
        double avgEnergy = entries.stream()
                .mapToInt(e -> e.getEnergyLevel() != null ? e.getEnergyLevel() : 0)
                .average()
                .orElse(0);
        if (avgEnergy < 2.5) {
            patterns.add("Consistently low energy levels");
        }

        // Check for poor sleep quality
        double avgSleep = entries.stream()
                .mapToInt(e -> e.getSleepQuality() != null ? e.getSleepQuality() : 0)
                .average()
                .orElse(0);
        if (avgSleep < 2.5) {
            patterns.add("Poor sleep quality detected");
        }

        return patterns;
    }

    /**
     * Generate recommendations based on mood data.
     * 
     * @param entries the mood entries
     * @param averageMoodRating the average mood rating
     * @return list of recommendations
     */
    private List<String> generateRecommendations(List<MoodEntry> entries, Double averageMoodRating) {
        List<String> recommendations = new ArrayList<>();

        if (entries.isEmpty()) {
            recommendations.add("Continue tracking your mood regularly");
            return recommendations;
        }

        // Recommendation for low mood
        if (averageMoodRating != null && averageMoodRating < 2.5) {
            recommendations.add("Consider scheduling a session with a counselor to discuss your mood");
            recommendations.add("Try engaging in activities that typically improve your mood");
        }

        // Recommendation for declining trend
        String trend = calculateTrendDirection(entries);
        if ("DECLINING".equals(trend)) {
            recommendations.add("Your mood has been declining - reach out for support");
        }

        // Recommendation for low energy
        double avgEnergy = entries.stream()
                .mapToInt(e -> e.getEnergyLevel() != null ? e.getEnergyLevel() : 0)
                .average()
                .orElse(0);
        if (avgEnergy < 2.5) {
            recommendations.add("Try incorporating more physical activity into your routine");
        }

        // Recommendation for poor sleep
        double avgSleep = entries.stream()
                .mapToInt(e -> e.getSleepQuality() != null ? e.getSleepQuality() : 0)
                .average()
                .orElse(0);
        if (avgSleep < 2.5) {
            recommendations.add("Focus on improving your sleep hygiene and sleep schedule");
        }

        // Positive reinforcement for good mood
        if (averageMoodRating != null && averageMoodRating >= 4) {
            recommendations.add("Great job maintaining a positive mood! Keep up the good work");
        }

        return recommendations;
    }

    /**
     * Get most common emotions.
     * 
     * @param emotionFrequency the emotion frequency map
     * @param limit the number of emotions to return
     * @return list of most common emotions
     */
    private List<String> getMostCommonEmotions(Map<String, Long> emotionFrequency, int limit) {
        return emotionFrequency.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Get highest mood rating.
     * 
     * @param entries the mood entries
     * @return highest mood rating or null if no entries
     */
    private Integer getHighestMoodRating(List<MoodEntry> entries) {
        return entries.stream()
                .map(MoodEntry::getMoodRating)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    /**
     * Get lowest mood rating.
     * 
     * @param entries the mood entries
     * @return lowest mood rating or null if no entries
     */
    private Integer getLowestMoodRating(List<MoodEntry> entries) {
        return entries.stream()
                .map(MoodEntry::getMoodRating)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(null);
    }

    /**
     * Verify that a counsellor has access to a student's mood data.
     * 
     * A counsellor can access a student's mood data if they have at least one
     * appointment with the student (completed or scheduled).
     * 
     * @param counsellorUserId the counsellor's user ID
     * @param studentId the student's user ID
     * @return true if the counsellor has access, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean verifyCounsellorStudentAccess(UUID counsellorUserId, UUID studentId) {
        if (counsellorUserId == null || studentId == null) {
            log.warn("Invalid parameters for access verification: counsellorUserId={}, studentId={}", 
                     counsellorUserId, studentId);
            return false;
        }

        // Check if there's at least one appointment between the counsellor and student
        long appointmentCount = appointmentRepository.countByCounsellorIdAndStudentId(counsellorUserId, studentId);
        
        boolean hasAccess = appointmentCount > 0;
        log.debug("Access verification for counsellor {} to student {}: {} (appointments: {})", 
                  counsellorUserId, studentId, hasAccess, appointmentCount);
        
        return hasAccess;
    }
}


