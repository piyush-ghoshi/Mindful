package com.mindful.wellness.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindful.wellness.dto.MoodAssessmentDto;
import com.mindful.wellness.dto.RiskAssessmentDto;
import com.mindful.wellness.entity.*;
import com.mindful.wellness.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for multi-factor risk assessment.
 * Evaluates suicide/self-harm risk based on various indicators.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeverityAssessmentService {
    
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final ObjectMapper objectMapper;
    
    // ── Risk Factor Weights (0-10) ─────────────────────────────────────────────
    private static final Map<String, Integer> RISK_WEIGHTS = Map.ofEntries(
        // Critical risks (9-10)
        Map.entry("suicidal_ideation", 10),
        Map.entry("suicide_plan", 10),
        Map.entry("suicide_intent", 10),
        
        // Severe risks (7-9)
        Map.entry("self_harm_intent", 9),
        Map.entry("active_self_harm", 9),
        Map.entry("means_access", 8),
        
        // High risks (5-7)
        Map.entry("hopelessness", 7),
        Map.entry("worthlessness", 6),
        Map.entry("severe_isolation", 5),
        Map.entry("substance_abuse", 6),
        Map.entry("recent_loss", 5),
        Map.entry("psychosis", 7),
        
        // Moderate risks (3-5)
        Map.entry("panic_symptoms", 4),
        Map.entry("dissociation", 5),
        Map.entry("chronic_pain", 4),
        Map.entry("insomnia", 3),
        
        // Lower risks (1-3)
        Map.entry("appetite_change", 2),
        Map.entry("social_withdrawal", 3),
        Map.entry("irritability", 2),
        Map.entry("concentration_problems", 2)
    );
    
    /**
     * Assess risk from message content and mood analysis.
     */
    public RiskAssessmentDto assessRisk(
            String messageContent,
            UUID sessionId,
            UUID messageId,
            UUID userId,
            MoodAssessmentDto moodAssessment) {
        
        log.debug("Assessing risk for message {} (user: {})", messageId, userId);
        
        // 1. Detect risk factors from content
        Map<String, Integer> detectedRiskFactors = detectRiskFactors(messageContent, moodAssessment);
        
        // 2. Calculate total risk score
        int totalRisk = calculateTotalRisk(detectedRiskFactors);
        
        // 3. Assess protective factors
        Map<String, Object> protectiveFactors = assessProtectiveFactors(messageContent, sessionId);
        
        // 4. Adjust score based on protective factors
        totalRisk = adjustForProtectiveFactors(totalRisk, protectiveFactors);
        
        // 5. Ensure score is in valid range [0, 10]
        totalRisk = Math.max(0, Math.min(10, totalRisk));
        
        // 6. Determine recommended action
        RecommendedAction recommendedAction = determineAction(totalRisk, detectedRiskFactors);
        
        // 7. Generate rationale
        String rationale = generateRationale(totalRisk, detectedRiskFactors, protectiveFactors);
        
        // 8. Save assessment
        RiskAssessment assessment = RiskAssessment.builder()
            .sessionId(sessionId)
            .messageId(messageId)
            .userId(userId)
            .totalRiskScore(totalRisk)
            .riskFactors(toJson(detectedRiskFactors))
            .protectiveFactors(toJson(protectiveFactors))
            .assessmentMethod(AnalysisMethod.HYBRID)
            .confidenceScore(BigDecimal.valueOf(0.80))
            .recommendedAction(recommendedAction)
            .rationale(rationale)
            .build();
        
        assessment = riskAssessmentRepository.save(assessment);
        
        log.info("Risk assessed for message {}: score={}/10, level={}, action={}", 
            messageId, totalRisk, assessment.getRiskLevel(), recommendedAction);
        
        return toDto(assessment);
    }
    
    /**
     * Detect risk factors from message content and mood.
     */
    private Map<String, Integer> detectRiskFactors(String content, MoodAssessmentDto mood) {
        Map<String, Integer> factors = new HashMap<>();
        String lower = content.toLowerCase();
        
        // ── Critical Risk Factors ──────────────────────────────────────────────
        
        // Suicidal ideation
        if (containsAny(lower, "kill myself", "end my life", "suicide", "want to die", 
                        "better off dead", "no reason to live")) {
            factors.put("suicidal_ideation", 10);
        }
        
        // Suicide plan
        if (containsAny(lower, "have a plan", "going to", "tonight", "pills", "rope",
                        "jump", "gun", "overdose")) {
            factors.put("suicide_plan", 10);
        }
        
        // Suicide intent
        if (containsAny(lower, "goodbye", "last time", "won't be here", "final message")) {
            factors.put("suicide_intent", 10);
        }
        
        // ── Severe Risk Factors ────────────────────────────────────────────────
        
        // Self-harm
        if (containsAny(lower, "cut myself", "hurt myself", "self-harm", "cutting",
                        "burning", "hitting")) {
            factors.put("self_harm_intent", 9);
        }
        
        // Active self-harm
        if (containsAny(lower, "just cut", "been cutting", "hurt myself today")) {
            factors.put("active_self_harm", 9);
        }
        
        // Means access
        if (containsAny(lower, "have pills", "have access", "got the")) {
            factors.put("means_access", 8);
        }
        
        // ── High Risk Factors ──────────────────────────────────────────────────
        
        // Hopelessness
        if (containsAny(lower, "no hope", "hopeless", "no point", "no future",
                        "never get better", "can't go on")) {
            factors.put("hopelessness", 7);
        }
        
        // Worthlessness
        if (containsAny(lower, "worthless", "burden", "better off without me",
                        "no value", "useless", "failure")) {
            factors.put("worthlessness", 6);
        }
        
        // Severe isolation
        if (containsAny(lower, "no one cares", "completely alone", "no friends",
                        "abandoned", "nobody")) {
            factors.put("severe_isolation", 5);
        }
        
        // Substance abuse
        if (containsAny(lower, "drinking", "drunk", "high", "drugs", "using")) {
            factors.put("substance_abuse", 6);
        }
        
        // ── Moderate Risk Factors ──────────────────────────────────────────────
        
        // Panic symptoms
        if (containsAny(lower, "can't breathe", "heart racing", "panic attack", "chest pain")) {
            factors.put("panic_symptoms", 4);
        }
        
        // Dissociation
        if (containsAny(lower, "feel numb", "disconnected", "not real", "watching myself")) {
            factors.put("dissociation", 5);
        }
        
        // ── Use mood assessment data ───────────────────────────────────────────
        
        if (mood != null) {
            // Hopeless mood is very concerning
            if (mood.getDetectedMood() == MoodType.HOPELESS) {
                factors.put("hopelessness", 7);
            }
            
            // Very negative sentiment
            if (mood.getSentimentScore() != null && 
                mood.getSentimentScore().compareTo(BigDecimal.valueOf(-0.7)) < 0) {
                factors.put("severe_distress", 5);
            }
            
            // High intensity negative mood
            if (mood.isHighIntensity() && mood.getDetectedMood().isNegative()) {
                factors.putIfAbsent("emotional_dysregulation", 4);
            }
            
            // Use AI-detected risk indicators
            if (mood.getRiskIndicators() != null) {
                for (String indicator : mood.getRiskIndicators()) {
                    Integer weight = RISK_WEIGHTS.get(indicator);
                    if (weight != null) {
                        factors.put(indicator, weight);
                    }
                }
            }
        }
        
        return factors;
    }
    
    /**
     * Calculate total risk score from factors.
     * Uses highest single factor, not sum (to avoid over-scoring).
     */
    private int calculateTotalRisk(Map<String, Integer> riskFactors) {
        if (riskFactors.isEmpty()) {
            return 0;
        }
        
        // Use highest risk factor
        int maxRisk = riskFactors.values().stream()
            .max(Integer::compareTo)
            .orElse(0);
        
        // Add bonus for multiple factors (up to +2)
        if (riskFactors.size() >= 5) {
            maxRisk = Math.min(10, maxRisk + 2);
        } else if (riskFactors.size() >= 3) {
            maxRisk = Math.min(10, maxRisk + 1);
        }
        
        return maxRisk;
    }
    
    /**
     * Assess protective factors.
     */
    private Map<String, Object> assessProtectiveFactors(String content, UUID sessionId) {
        Map<String, Object> factors = new HashMap<>();
        String lower = content.toLowerCase();
        
        // Social support
        boolean hasSocialSupport = containsAny(lower,
            "my friends", "my family", "talked to", "support", "someone I trust",
            "partner", "boyfriend", "girlfriend", "spouse");
        factors.put("social_support", hasSocialSupport);
        
        // Coping skills
        boolean usesCoping = containsAny(lower,
            "meditation", "exercise", "therapy", "breathing", "journal",
            "walk", "music", "hobbies", "pets");
        factors.put("coping_skills", usesCoping);
        
        // Hope/future orientation
        boolean hasHope = containsAny(lower,
            "hopefully", "tomorrow", "next week", "plan to", "looking forward",
            "want to", "going to try", "maybe");
        factors.put("future_orientation", hasHope);
        
        // Help-seeking behavior
        boolean seekingHelp = containsAny(lower,
            "need help", "talk to someone", "see a counsellor", "appointment",
            "therapist", "doctor", "help me");
        factors.put("help_seeking", seekingHelp);
        
        // Treatment engagement
        boolean inTreatment = containsAny(lower,
            "my therapist", "my counsellor", "in therapy", "taking medication",
            "seeing a doctor");
        factors.put("treatment_engagement", inTreatment);
        
        // Reasons for living
        boolean hasReasons = containsAny(lower,
            "for my", "don't want to hurt", "people care", "family needs me",
            "pet", "children", "kids");
        factors.put("reasons_for_living", hasReasons);
        
        return factors;
    }
    
    /**
     * Adjust risk score based on protective factors.
     */
    private int adjustForProtectiveFactors(int riskScore, Map<String, Object> protective) {
        int adjustment = 0;
        
        if (Boolean.TRUE.equals(protective.get("social_support")))
            adjustment++;
        if (Boolean.TRUE.equals(protective.get("coping_skills")))
            adjustment++;
        if (Boolean.TRUE.equals(protective.get("future_orientation")))
            adjustment += 2; // Strong protective factor
        if (Boolean.TRUE.equals(protective.get("help_seeking")))
            adjustment++;
        if (Boolean.TRUE.equals(protective.get("treatment_engagement")))
            adjustment += 2; // Strong protective factor
        if (Boolean.TRUE.equals(protective.get("reasons_for_living")))
            adjustment += 2; // Very strong protective factor
        
        // Cap reduction at 3 points (don't reduce critical risk too much)
        adjustment = Math.min(3, adjustment);
        
        // Never reduce below 0
        return Math.max(0, riskScore - adjustment);
    }
    
    /**
     * Determine recommended action based on risk.
     */
    private RecommendedAction determineAction(int riskScore, Map<String, Integer> riskFactors) {
        // Critical: suicide plan or intent
        if (riskFactors.containsKey("suicide_plan") || 
            riskFactors.containsKey("suicide_intent"))
            return RecommendedAction.CRISIS_INTERVENTION;
        
        // Critical: very high score
        if (riskScore >= 9)
            return RecommendedAction.CRISIS_INTERVENTION;
        
        // Severe: suicidal ideation or high score
        if (riskFactors.containsKey("suicidal_ideation") || riskScore >= 8)
            return RecommendedAction.URGENT_COUNSELLOR_ALERT;
        
        // High: high score or self-harm
        if (riskScore >= 7 || riskFactors.containsKey("self_harm_intent"))
            return RecommendedAction.SAME_DAY_APPOINTMENT;
        
        // Moderate: moderate score
        if (riskScore >= 5)
            return RecommendedAction.BOOK_APPOINTMENT_SOON;
        
        // Low: low-moderate score
        if (riskScore >= 3)
            return RecommendedAction.BOOK_APPOINTMENT_LATER;
        
        // Minimal: low score
        if (riskScore >= 1)
            return RecommendedAction.CONTINUE_MONITORING;
        
        return RecommendedAction.PREVENTIVE_CARE;
    }
    
    /**
     * Generate rationale for assessment.
     */
    private String generateRationale(
            int riskScore,
            Map<String, Integer> riskFactors,
            Map<String, Object> protectiveFactors) {
        
        StringBuilder rationale = new StringBuilder();
        
        // Risk level
        RiskLevel level = RiskLevel.fromScore(riskScore);
        rationale.append(String.format("Risk Level: %s (%d/10). ", level.getDisplayName(), riskScore));
        
        // Risk factors
        if (!riskFactors.isEmpty()) {
            rationale.append("Detected factors: ");
            rationale.append(riskFactors.keySet().stream()
                .map(f -> f.replace("_", " "))
                .collect(Collectors.joining(", ")));
            rationale.append(". ");
        }
        
        // Protective factors
        long protectiveCount = protectiveFactors.values().stream()
            .filter(v -> Boolean.TRUE.equals(v))
            .count();
        
        if (protectiveCount > 0) {
            rationale.append(String.format("Protective factors present: %d. ", protectiveCount));
        } else {
            rationale.append("Limited protective factors detected. ");
        }
        
        return rationale.toString();
    }
    
    /**
     * Get risk trend for user.
     */
    @Transactional(readOnly = true)
    public String getRiskTrend(UUID userId, LocalDateTime since) {
        List<Integer> scores = riskAssessmentRepository.getRiskScoreTrend(userId, since);
        
        if (scores.size() < 3) {
            return "INSUFFICIENT_DATA";
        }
        
        // Compare first half vs second half
        int mid = scores.size() / 2;
        double firstHalf = scores.subList(0, mid).stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0);
        
        double secondHalf = scores.subList(mid, scores.size()).stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0);
        
        if (secondHalf > firstHalf + 1.5)
            return "WORSENING";
        else if (secondHalf < firstHalf - 1.5)
            return "IMPROVING";
        else
            return "STABLE";
    }
    
    // ── Helper Methods ─────────────────────────────────────────────────────────
    
    private boolean containsAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }
    
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize to JSON: {}", e.getMessage());
            return "{}";
        }
    }
    
    private RiskAssessmentDto toDto(RiskAssessment assessment) {
        Map<String, Integer> riskFactors = fromJson(assessment.getRiskFactors(), Map.class);
        Map<String, Object> protectiveFactors = fromJson(assessment.getProtectiveFactors(), Map.class);
        
        return RiskAssessmentDto.builder()
            .id(assessment.getId())
            .sessionId(assessment.getSessionId())
            .messageId(assessment.getMessageId())
            .userId(assessment.getUserId())
            .totalRiskScore(assessment.getTotalRiskScore())
            .riskLevel(assessment.getRiskLevel())
            .riskFactors(riskFactors != null ? riskFactors : Map.of())
            .protectiveFactors(protectiveFactors != null ? protectiveFactors : Map.of())
            .assessmentMethod(assessment.getAssessmentMethod())
            .confidenceScore(assessment.getConfidenceScore())
            .recommendedAction(assessment.getRecommendedAction())
            .rationale(assessment.getRationale())
            .createdAt(assessment.getCreatedAt())
            .isCrisis(assessment.isCrisis())
            .requiresImmediateAction(assessment.requiresImmediateAction())
            .description(assessment.getDescription())
            .build();
    }
    
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
