package com.mindful.wellness.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindful.wellness.dto.MoodAnalysisResult;
import com.mindful.wellness.dto.MoodAssessmentDto;
import com.mindful.wellness.dto.MoodShiftDto;
import com.mindful.wellness.dto.MoodTrajectoryDto;
import com.mindful.wellness.entity.AnalysisMethod;
import com.mindful.wellness.entity.MoodAssessment;
import com.mindful.wellness.entity.MoodType;
import com.mindful.wellness.repository.MoodAssessmentRepository;
import com.mindful.wellness.util.SentimentAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analyzing mood and sentiment from chat messages.
 * 
 * Provides:
 * - Mood detection (keyword-based and AI-powered)
 * - Sentiment analysis
 * - Intensity calculation
 * - Mood trajectory tracking
 * - Mood shift detection
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MoodAnalysisService {
    
    private final MoodAssessmentRepository moodAssessmentRepository;
    private final GroqService groqService;
    private final ObjectMapper objectMapper;
    
    // Mood detection keywords
    private static final Map<MoodType, Set<String>> MOOD_KEYWORDS = Map.ofEntries(
        Map.entry(MoodType.HAPPY, Set.of(
            "happy", "joyful", "excited", "great", "wonderful", "amazing",
            "thrilled", "pleased", "content", "cheerful", "delighted"
        )),
        Map.entry(MoodType.SAD, Set.of(
            "sad", "depressed", "down", "low", "unhappy", "miserable",
            "gloomy", "dejected", "melancholy", "blue", "heartbroken"
        )),
        Map.entry(MoodType.ANXIOUS, Set.of(
            "anxious", "worried", "nervous", "panicking", "scared",
            "afraid", "fearful", "terrified", "uneasy", "tense", "on edge"
        )),
        Map.entry(MoodType.ANGRY, Set.of(
            "angry", "mad", "furious", "enraged", "irritated",
            "annoyed", "frustrated", "infuriated", "livid", "outraged"
        )),
        Map.entry(MoodType.FRUSTRATED, Set.of(
            "frustrated", "stuck", "annoyed", "irritated", "fed up",
            "exasperated", "agitated", "bothered", "vexed"
        )),
        Map.entry(MoodType.HOPEFUL, Set.of(
            "hopeful", "optimistic", "positive", "encouraged",
            "confident", "expecting", "looking forward", "promising"
        )),
        Map.entry(MoodType.HOPELESS, Set.of(
            "hopeless", "no hope", "no point", "giving up", "pointless",
            "futile", "despairing", "desperate", "no way out"
        )),
        Map.entry(MoodType.CALM, Set.of(
            "calm", "peaceful", "relaxed", "serene", "tranquil",
            "composed", "collected", "at ease", "comfortable"
        )),
        Map.entry(MoodType.OVERWHELMED, Set.of(
            "overwhelmed", "stressed", "too much", "can't cope",
            "drowning", "buried", "swamped", "overloaded"
        ))
    );
    
    /**
     * Analyze mood from a chat message.
     * Uses hybrid approach: Try AI first, fallback to keyword-based.
     * 
     * @param messageContent The message text to analyze
     * @param sessionId The chat session ID
     * @param messageId The message ID
     * @param userId The user ID
     * @return MoodAssessmentDto with detected mood and sentiment
     */
    public MoodAssessmentDto analyzeMood(String messageContent, UUID sessionId, UUID messageId, UUID userId) {
        log.debug("Analyzing mood for message {} (user: {})", messageId, userId);
        
        // Get conversation context (last 5 user messages for AI)
        List<String> context = getRecentMessageContext(sessionId, 5);
        
        // Try AI-based analysis first
        MoodAnalysisResult aiResult = groqService.analyzeMoodWithAI(messageContent, context);
        
        if (aiResult != null && isValidAIResult(aiResult)) {
            // Use AI result
            log.info("Using AI-based mood analysis (confidence: {})", aiResult.getConfidence());
            return saveAIMoodAssessment(sessionId, messageId, userId, aiResult);
        } else {
            // Fallback to keyword-based analysis
            log.info("AI analysis unavailable, using keyword-based fallback");
            return analyzeKeywordBased(messageContent, sessionId, messageId, userId);
        }
    }
    
    /**
     * Analyze mood using keyword-based method (fallback).
     */
    private MoodAssessmentDto analyzeKeywordBased(String messageContent, UUID sessionId, UUID messageId, UUID userId) {
        // 1. Detect mood using keywords
        MoodType detectedMood = detectMoodFromKeywords(messageContent);
        
        // 2. Calculate sentiment score
        BigDecimal sentimentScore = SentimentAnalyzer.calculateSentiment(messageContent);
        
        // 3. Calculate mood intensity
        BigDecimal intensity = SentimentAnalyzer.calculateIntensity(messageContent, detectedMood.name());
        
        // 4. Extract dominant emotions
        List<String> dominantEmotions = extractDominantEmotions(messageContent, detectedMood);
        
        // 5. Create and save assessment
        MoodAssessment assessment = MoodAssessment.builder()
            .sessionId(sessionId)
            .messageId(messageId)
            .userId(userId)
            .detectedMood(detectedMood)
            .moodIntensity(intensity)
            .sentimentScore(sentimentScore)
            .dominantEmotions(toJson(dominantEmotions))
            .analysisMethod(AnalysisMethod.KEYWORD)
            .confidenceScore(BigDecimal.valueOf(0.70)) // Lower confidence for keyword-based
            .build();
        
        assessment = moodAssessmentRepository.save(assessment);
        
        log.info("Mood analyzed (KEYWORD): {} (intensity: {}, sentiment: {}) for message {}", 
            detectedMood, intensity, sentimentScore, messageId);
        
        return toDto(assessment);
    }
    
    /**
     * Save AI-based mood assessment.
     */
    private MoodAssessmentDto saveAIMoodAssessment(UUID sessionId, UUID messageId, UUID userId, MoodAnalysisResult aiResult) {
        // Map AI mood string to MoodType enum
        MoodType moodType = mapAIMoodToEnum(aiResult.getMood());
        
        MoodAssessment assessment = MoodAssessment.builder()
            .sessionId(sessionId)
            .messageId(messageId)
            .userId(userId)
            .detectedMood(moodType)
            .moodIntensity(aiResult.getMoodIntensity())
            .sentimentScore(aiResult.getSentimentScore())
            .dominantEmotions(toJson(aiResult.getDominantEmotions()))
            .riskIndicators(toJson(aiResult.getRiskIndicators()))
            .analysisMethod(AnalysisMethod.AI)
            .confidenceScore(aiResult.getConfidence() != null ? aiResult.getConfidence() : BigDecimal.valueOf(0.85))
            .build();
        
        assessment = moodAssessmentRepository.save(assessment);
        
        log.info("Mood analyzed (AI): {} (intensity: {}, sentiment: {}, confidence: {}) for message {}", 
            moodType, aiResult.getMoodIntensity(), aiResult.getSentimentScore(), 
            aiResult.getConfidence(), messageId);
        
        return toDto(assessment);
    }
    
    /**
     * Validate AI result has required fields.
     */
    private boolean isValidAIResult(MoodAnalysisResult result) {
        if (result == null) return false;
        if (result.getMood() == null || result.getMood().trim().isEmpty()) return false;
        if (result.getMoodIntensity() == null) return false;
        if (result.getSentimentScore() == null) return false;
        
        // Check confidence threshold (only use AI if confidence >= 0.6)
        if (result.getConfidence() != null && result.getConfidence().compareTo(BigDecimal.valueOf(0.6)) < 0) {
            log.warn("AI confidence too low: {}, using fallback", result.getConfidence());
            return false;
        }
        
        return true;
    }
    
    /**
     * Map AI mood string to MoodType enum.
     */
    private MoodType mapAIMoodToEnum(String aiMood) {
        if (aiMood == null) return MoodType.NEUTRAL;
        
        try {
            return MoodType.valueOf(aiMood.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown AI mood '{}', defaulting to NEUTRAL", aiMood);
            return MoodType.NEUTRAL;
        }
    }
    
    /**
     * Get recent message context for AI analysis.
     */
    private List<String> getRecentMessageContext(UUID sessionId, int limit) {
        try {
            List<MoodAssessment> recent = moodAssessmentRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId);
            
            return recent.stream()
                .limit(limit)
                .map(a -> String.format("%s (sentiment: %.2f)", 
                    a.getDetectedMood().getDisplayName(), 
                    a.getSentimentScore()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not fetch context: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get mood trajectory for a session.
     * Shows how mood changes over the conversation.
     */
    @Transactional(readOnly = true)
    public MoodTrajectoryDto getSessionMoodTrajectory(UUID sessionId) {
        List<MoodAssessment> assessments = moodAssessmentRepository
            .findBySessionIdOrderByCreatedAtAsc(sessionId);
        
        if (assessments.isEmpty()) {
            return MoodTrajectoryDto.builder()
                .sessionId(sessionId)
                .dataPoints(List.of())
                .averageSentiment(BigDecimal.ZERO)
                .averageIntensity(BigDecimal.valueOf(0.5))
                .dominantMood("NEUTRAL")
                .trend("STABLE")
                .insights(List.of("No mood data available yet"))
                .hasConcerningPattern(false)
                .significantShifts(List.of())
                .build();
        }
        
        // Build data points
        List<MoodTrajectoryDto.MoodDataPoint> dataPoints = new ArrayList<>();
        for (int i = 0; i < assessments.size(); i++) {
            MoodAssessment a = assessments.get(i);
            dataPoints.add(MoodTrajectoryDto.MoodDataPoint.builder()
                .timestamp(a.getCreatedAt())
                .mood(a.getDetectedMood().name())
                .intensity(a.getMoodIntensity())
                .sentiment(a.getSentimentScore())
                .messageIndex(i + 1)
                .build());
        }
        
        // Calculate statistics
        BigDecimal avgSentiment = assessments.stream()
            .map(MoodAssessment::getSentimentScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(assessments.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal avgIntensity = assessments.stream()
            .map(MoodAssessment::getMoodIntensity)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(assessments.size()), 2, RoundingMode.HALF_UP);
        
        // Determine dominant mood
        Map<MoodType, Long> moodCounts = assessments.stream()
            .collect(Collectors.groupingBy(MoodAssessment::getDetectedMood, Collectors.counting()));
        String dominantMood = moodCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(e -> e.getKey().name())
            .orElse("NEUTRAL");
        
        // Determine trend
        String trend = calculateTrend(assessments);
        
        // Generate insights
        List<String> insights = generateInsights(assessments, avgSentiment, avgIntensity, trend);
        
        // Detect significant shifts
        List<MoodShiftDto> shifts = detectMoodShifts(assessments);
        
        // Check for concerning patterns
        boolean hasConcerningPattern = checkConcerningPattern(assessments);
        
        return MoodTrajectoryDto.builder()
            .sessionId(sessionId)
            .userId(assessments.get(0).getUserId())
            .dataPoints(dataPoints)
            .averageSentiment(avgSentiment)
            .averageIntensity(avgIntensity)
            .dominantMood(dominantMood)
            .trend(trend)
            .insights(insights)
            .hasConcerningPattern(hasConcerningPattern)
            .significantShifts(shifts)
            .build();
    }
    
    /**
     * Detect significant mood shifts in a session.
     */
    public List<MoodShiftDto> detectMoodShifts(UUID sessionId) {
        List<MoodAssessment> assessments = moodAssessmentRepository
            .findBySessionIdOrderByCreatedAtAsc(sessionId);
        return detectMoodShifts(assessments);
    }
    
    // ── Private Helper Methods ─────────────────────────────────────────────────
    
    /**
     * Detect mood from keywords (baseline method).
     */
    private MoodType detectMoodFromKeywords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return MoodType.NEUTRAL;
        }
        
        String lower = content.toLowerCase();
        
        // Count matches for each mood
        Map<MoodType, Integer> moodScores = new HashMap<>();
        
        for (Map.Entry<MoodType, Set<String>> entry : MOOD_KEYWORDS.entrySet()) {
            int score = (int) entry.getValue().stream()
                .filter(lower::contains)
                .count();
            if (score > 0) {
                moodScores.put(entry.getKey(), score);
            }
        }
        
        // Return mood with highest score
        if (moodScores.isEmpty()) {
            return MoodType.NEUTRAL;
        }
        
        return moodScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(MoodType.NEUTRAL);
    }
    
    /**
     * Extract 2-4 dominant emotions from text.
     */
    private List<String> extractDominantEmotions(String content, MoodType primaryMood) {
        List<String> emotions = new ArrayList<>();
        String lower = content.toLowerCase();
        
        // Add primary mood
        emotions.add(primaryMood.getDisplayName().toLowerCase());
        
        // Add related emotions based on keywords
        if (lower.contains("tired") || lower.contains("exhausted") || lower.contains("fatigue")) {
            emotions.add("fatigue");
        }
        if (lower.contains("worry") || lower.contains("concern")) {
            emotions.add("worry");
        }
        if (lower.contains("fear") || lower.contains("scared")) {
            emotions.add("fear");
        }
        if (lower.contains("guilt") || lower.contains("ashamed")) {
            emotions.add("guilt");
        }
        if (lower.contains("lonely") || lower.contains("alone")) {
            emotions.add("loneliness");
        }
        
        // Return 2-4 emotions
        return emotions.stream().distinct().limit(4).collect(Collectors.toList());
    }
    
    /**
     * Calculate trend (IMPROVING, DECLINING, STABLE).
     */
    private String calculateTrend(List<MoodAssessment> assessments) {
        if (assessments.size() < 3) {
            return "STABLE";
        }
        
        // Compare first half vs second half sentiment
        int midpoint = assessments.size() / 2;
        
        double firstHalfAvg = assessments.subList(0, midpoint).stream()
            .mapToDouble(a -> a.getSentimentScore().doubleValue())
            .average()
            .orElse(0.0);
        
        double secondHalfAvg = assessments.subList(midpoint, assessments.size()).stream()
            .mapToDouble(a -> a.getSentimentScore().doubleValue())
            .average()
            .orElse(0.0);
        
        double change = secondHalfAvg - firstHalfAvg;
        
        if (change > 0.2) return "IMPROVING";
        if (change < -0.2) return "DECLINING";
        return "STABLE";
    }
    
    /**
     * Generate insights from mood data.
     */
    private List<String> generateInsights(
            List<MoodAssessment> assessments,
            BigDecimal avgSentiment,
            BigDecimal avgIntensity,
            String trend) {
        
        List<String> insights = new ArrayList<>();
        
        // Sentiment insight
        if (avgSentiment.compareTo(BigDecimal.valueOf(-0.5)) < 0) {
            insights.add("Overall sentiment is quite negative. You're going through a difficult time.");
        } else if (avgSentiment.compareTo(BigDecimal.ZERO) < 0) {
            insights.add("Sentiment is somewhat negative, but there's room for improvement.");
        } else if (avgSentiment.compareTo(BigDecimal.valueOf(0.3)) > 0) {
            insights.add("Overall sentiment is positive! You're managing well.");
        }
        
        // Intensity insight
        if (avgIntensity.compareTo(BigDecimal.valueOf(0.7)) > 0) {
            insights.add("Your emotions are quite intense. Consider grounding techniques.");
        }
        
        // Trend insight
        if ("IMPROVING".equals(trend)) {
            insights.add("Good news! Your mood is improving over the conversation.");
        } else if ("DECLINING".equals(trend)) {
            insights.add("Your mood seems to be declining. Let's explore what's troubling you.");
        }
        
        // Pattern insights
        long negativeCount = assessments.stream()
            .filter(a -> a.getDetectedMood().isNegative())
            .count();
        
        if (negativeCount > assessments.size() * 0.7) {
            insights.add("Most of your messages show distress. You're not alone, and support is available.");
        }
        
        return insights;
    }
    
    /**
     * Detect mood shifts in conversation.
     */
    private List<MoodShiftDto> detectMoodShifts(List<MoodAssessment> assessments) {
        List<MoodShiftDto> shifts = new ArrayList<>();
        
        if (assessments.size() < 2) {
            return shifts;
        }
        
        for (int i = 1; i < assessments.size(); i++) {
            MoodAssessment prev = assessments.get(i - 1);
            MoodAssessment curr = assessments.get(i);
            
            BigDecimal sentimentChange = curr.getSentimentScore().subtract(prev.getSentimentScore());
            
            // Significant shift: change > 0.4
            if (sentimentChange.abs().compareTo(BigDecimal.valueOf(0.4)) > 0) {
                String shiftType = sentimentChange.compareTo(BigDecimal.ZERO) > 0 ? "POSITIVE" : "NEGATIVE";
                String severity = sentimentChange.abs().compareTo(BigDecimal.valueOf(0.6)) > 0 ? "MAJOR" : "MODERATE";
                
                String description = String.format(
                    "Mood shifted from %s to %s (sentiment %s by %.2f)",
                    prev.getDetectedMood().getDisplayName(),
                    curr.getDetectedMood().getDisplayName(),
                    shiftType.equals("POSITIVE") ? "improved" : "worsened",
                    sentimentChange.abs()
                );
                
                shifts.add(MoodShiftDto.builder()
                    .fromTime(prev.getCreatedAt())
                    .toTime(curr.getCreatedAt())
                    .fromMood(prev.getDetectedMood().name())
                    .toMood(curr.getDetectedMood().name())
                    .fromSentiment(prev.getSentimentScore())
                    .toSentiment(curr.getSentimentScore())
                    .sentimentChange(sentimentChange)
                    .shiftType(shiftType)
                    .severity(severity)
                    .description(description)
                    .isConcerning("NEGATIVE".equals(shiftType) && "MAJOR".equals(severity))
                    .build());
            }
        }
        
        return shifts;
    }
    
    /**
     * Check if there's a concerning pattern in mood assessments.
     */
    private boolean checkConcerningPattern(List<MoodAssessment> assessments) {
        if (assessments.size() < 3) {
            return false;
        }
        
        // Pattern 1: Multiple consecutive high-intensity negative moods
        int consecutiveNegative = 0;
        for (MoodAssessment a : assessments) {
            if (a.getDetectedMood().isNegative() && 
                a.getMoodIntensity().compareTo(BigDecimal.valueOf(0.6)) > 0) {
                consecutiveNegative++;
                if (consecutiveNegative >= 3) {
                    return true;
                }
            } else {
                consecutiveNegative = 0;
            }
        }
        
        // Pattern 2: Any HOPELESS mood
        boolean hasHopelessness = assessments.stream()
            .anyMatch(a -> a.getDetectedMood() == MoodType.HOPELESS);
        
        // Pattern 3: Consistently declining sentiment
        boolean isConsistentlyDeclining = "DECLINING".equals(calculateTrend(assessments));
        BigDecimal avgSentiment = assessments.stream()
            .map(MoodAssessment::getSentimentScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(assessments.size()), 2, RoundingMode.HALF_UP);
        
        return hasHopelessness || (isConsistentlyDeclining && avgSentiment.compareTo(BigDecimal.valueOf(-0.3)) < 0);
    }
    
    /**
     * Convert entity to DTO.
     */
    private MoodAssessmentDto toDto(MoodAssessment assessment) {
        List<String> emotions = fromJson(assessment.getDominantEmotions(), List.class);
        List<String> risks = fromJson(assessment.getRiskIndicators(), List.class);
        
        return MoodAssessmentDto.builder()
            .id(assessment.getId())
            .sessionId(assessment.getSessionId())
            .messageId(assessment.getMessageId())
            .userId(assessment.getUserId())
            .detectedMood(assessment.getDetectedMood())
            .moodIntensity(assessment.getMoodIntensity())
            .sentimentScore(assessment.getSentimentScore())
            .dominantEmotions(emotions != null ? emotions : List.of())
            .analysisMethod(assessment.getAnalysisMethod())
            .confidenceScore(assessment.getConfidenceScore())
            .riskIndicators(risks != null ? risks : List.of())
            .createdAt(assessment.getCreatedAt())
            .moodDescription(assessment.getDescription())
            .isConcerning(assessment.isConcerning())
            .isHighIntensity(assessment.isHighIntensity())
            .isNegativeSentiment(assessment.isNegativeSentiment())
            .build();
    }
    
    /**
     * Convert object to JSON string.
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize to JSON: {}", e.getMessage());
            return "[]";
        }
    }
    
    /**
     * Parse JSON string to object.
     */
    private <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", e.getMessage());
            return null;
        }
    }
}
