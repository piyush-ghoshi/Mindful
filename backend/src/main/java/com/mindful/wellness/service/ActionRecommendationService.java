package com.mindful.wellness.service;

import com.mindful.wellness.dto.ActionRecommendationDto;
import com.mindful.wellness.dto.RiskAssessmentDto;
import com.mindful.wellness.entity.*;
import com.mindful.wellness.repository.ActionRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating and managing action recommendations.
 * Creates personalized intervention suggestions based on risk assessment.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ActionRecommendationService {
    
    private final ActionRecommendationRepository actionRecommendationRepository;
    
    /**
     * Generate personalized action recommendations based on risk assessment.
     */
    public List<ActionRecommendationDto> generateRecommendations(
            RiskAssessmentDto riskAssessment,
            UUID userId,
            UUID sessionId) {
        
        log.debug("Generating recommendations for risk score {} (level: {})", 
            riskAssessment.getTotalRiskScore(),
            riskAssessment.getRiskLevel());
        
        List<ActionRecommendation> recommendations = new ArrayList<>();
        
        // Determine priority based on risk
        ActionPriority priority = ActionPriority.fromRiskScore(riskAssessment.getTotalRiskScore());
        
        // Generate recommendations based on risk level and factors
        if (riskAssessment.isCrisis()) {
            recommendations.addAll(generateCrisisRecommendations(riskAssessment, userId, sessionId, priority));
        } else if (riskAssessment.getTotalRiskScore() >= 7) {
            recommendations.addAll(generateHighRiskRecommendations(riskAssessment, userId, sessionId, priority));
        } else if (riskAssessment.getTotalRiskScore() >= 5) {
            recommendations.addAll(generateModerateRiskRecommendations(riskAssessment, userId, sessionId, priority));
        } else if (riskAssessment.getTotalRiskScore() >= 3) {
            recommendations.addAll(generateLowRiskRecommendations(riskAssessment, userId, sessionId, priority));
        } else {
            recommendations.addAll(generateWellnessRecommendations(riskAssessment, userId, sessionId, priority));
        }
        
        // Add recommendations based on specific risk factors
        if (riskAssessment.getRiskFactors() != null) {
            recommendations.addAll(generateFactorSpecificRecommendations(
                riskAssessment, userId, sessionId, priority
            ));
        }
        
        // Save all recommendations
        recommendations = actionRecommendationRepository.saveAll(recommendations);
        
        log.info("Generated {} recommendations for user {} (risk: {}/10)", 
            recommendations.size(), userId, riskAssessment.getTotalRiskScore());
        
        return recommendations.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Crisis-level recommendations (risk 9-10).
     */
    private List<ActionRecommendation> generateCrisisRecommendations(
            RiskAssessmentDto risk, UUID userId, UUID sessionId, ActionPriority priority) {
        
        List<ActionRecommendation> recs = new ArrayList<>();
        
        // 1. Call crisis helpline immediately
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.CALL_CRISIS_HELPLINE)
            .priority(ActionPriority.CRITICAL)
            .title("Call Crisis Helpline Now")
            .description("You're in crisis and need immediate support. Please call a crisis helpline right now.")
            .specificAction("**Immediate action:**\n\n" +
                "• **iCall (India):** 9152987821\n" +
                "• **Vandrevala Foundation:** 1860-2662-345 (24/7)\n" +
                "• **AASRA:** 9820466627 (24/7)\n" +
                "• **Or go to nearest emergency room**\n\n" +
                "You are not alone. Help is available right now.")
            .resourceType("HELPLINE")
            .expiresAt(LocalDateTime.now().plusHours(2)) // Time-sensitive
            .build());
        
        // 2. Alert emergency contact
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.ALERT_EMERGENCY_CONTACT)
            .priority(ActionPriority.CRITICAL)
            .title("Contact Someone You Trust")
            .description("Please reach out to a family member, friend, or trusted person immediately.")
            .specificAction("Call or text someone who cares about you. Let them know you're struggling and need support right now.")
            .expiresAt(LocalDateTime.now().plusHours(6))
            .build());
        
        // 3. Create safety plan
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.CREATE_SAFETY_PLAN)
            .priority(ActionPriority.CRITICAL)
            .title("Create Your Safety Plan")
            .description("A safety plan helps you stay safe during difficult moments.")
            .specificAction("1. Remove harmful items from immediate access\n" +
                "2. Identify 3 people you can call\n" +
                "3. List coping strategies that have helped before\n" +
                "4. Reasons for living\n" +
                "5. Emergency numbers saved in phone")
            .build());
        
        return recs;
    }
    
    /**
     * High-risk recommendations (risk 7-8).
     */
    private List<ActionRecommendation> generateHighRiskRecommendations(
            RiskAssessmentDto risk, UUID userId, UUID sessionId, ActionPriority priority) {
        
        List<ActionRecommendation> recs = new ArrayList<>();
        
        // 1. Book urgent appointment
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.BOOK_URGENT_APPOINTMENT)
            .priority(ActionPriority.HIGH)
            .title("Book Urgent Counselling Appointment")
            .description("Your current state requires professional support within 24-48 hours.")
            .specificAction("Go to the Appointments section and book the earliest available slot with a counsellor. " +
                "Explain this is urgent when booking.")
            .resourceUrl("/appointments")
            .expiresAt(LocalDateTime.now().plusDays(2))
            .build());
        
        // 2. Call counsellor now
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.CALL_COUNSELLOR_NOW)
            .priority(ActionPriority.HIGH)
            .title("Speak with On-Call Counsellor")
            .description("An on-call counsellor is available to talk with you now.")
            .specificAction("Call the campus counselling center or use the emergency counsellor hotline provided by your institution.")
            .resourceType("HELPLINE")
            .expiresAt(LocalDateTime.now().plusHours(12))
            .build());
        
        // 3. Reach out to someone
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.REACH_OUT_TO_FRIEND)
            .priority(ActionPriority.HIGH)
            .title("Connect with Someone You Trust")
            .description("Social support is crucial right now. Don't go through this alone.")
            .specificAction("Call or meet with a friend, family member, or trusted person today. " +
                "You don't have to share everything, but let them know you're struggling.")
            .build());
        
        return recs;
    }
    
    /**
     * Moderate-risk recommendations (risk 5-6).
     */
    private List<ActionRecommendation> generateModerateRiskRecommendations(
            RiskAssessmentDto risk, UUID userId, UUID sessionId, ActionPriority priority) {
        
        List<ActionRecommendation> recs = new ArrayList<>();
        
        // 1. Book appointment
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.BOOK_APPOINTMENT)
            .priority(ActionPriority.MEDIUM)
            .title("Schedule a Counselling Session")
            .description("Professional support can help you work through what you're experiencing.")
            .specificAction("Book an appointment with a counsellor this week. They can provide strategies and support.")
            .resourceUrl("/appointments")
            .build());
        
        // 2. Practice grounding exercise
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.DO_GROUNDING_EXERCISE)
            .priority(ActionPriority.MEDIUM)
            .title("Try a Grounding Exercise")
            .description("Grounding techniques help when feelings become overwhelming.")
            .specificAction("**5-4-3-2-1 Technique:**\n" +
                "• Name 5 things you can see\n" +
                "• 4 things you can touch\n" +
                "• 3 things you can hear\n" +
                "• 2 things you can smell\n" +
                "• 1 thing you can taste\n\n" +
                "This brings you back to the present moment.")
            .resourceType("EXERCISE")
            .build());
        
        // 3. Daily mood tracking
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.TRACK_MOOD_DAILY)
            .priority(ActionPriority.MEDIUM)
            .title("Track Your Mood Daily")
            .description("Monitoring your mental state helps identify patterns and triggers.")
            .specificAction("Use the mood tracker feature to log how you're feeling each day. " +
                "Note what helps and what makes things harder.")
            .resourceUrl("/mood-tracker")
            .build());
        
        return recs;
    }
    
    /**
     * Low-risk recommendations (risk 3-4).
     */
    private List<ActionRecommendation> generateLowRiskRecommendations(
            RiskAssessmentDto risk, UUID userId, UUID sessionId, ActionPriority priority) {
        
        List<ActionRecommendation> recs = new ArrayList<>();
        
        // 1. Breathing exercise
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.DO_BREATHING_EXERCISE)
            .priority(ActionPriority.MEDIUM)
            .title("Practice Box Breathing")
            .description("Calm your nervous system with simple breathing.")
            .specificAction("**Box Breathing (4-4-4-4):**\n" +
                "• Inhale for 4 seconds\n" +
                "• Hold for 4 seconds\n" +
                "• Exhale for 4 seconds\n" +
                "• Hold for 4 seconds\n\n" +
                "Repeat 5 times. Do this whenever you feel anxious or stressed.")
            .resourceType("EXERCISE")
            .build());
        
        // 2. Physical exercise
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.DO_PHYSICAL_EXERCISE)
            .priority(ActionPriority.LOW)
            .title("Get Moving - Exercise")
            .description("Physical activity has powerful effects on mood and anxiety.")
            .specificAction("Go for a 15-30 minute walk, do yoga, or any movement you enjoy. " +
                "Even light exercise helps.")
            .resourceType("EXERCISE")
            .resourceUrl("/exercises")
            .build());
        
        // 3. Social connection
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.REACH_OUT_TO_FRIEND)
            .priority(ActionPriority.LOW)
            .title("Connect with Friends")
            .description("Social interaction helps improve mood and reduces isolation.")
            .specificAction("Reach out to a friend for a chat, coffee, or activity you both enjoy.")
            .build());
        
        return recs;
    }
    
    /**
     * Wellness recommendations (risk 0-2).
     */
    private List<ActionRecommendation> generateWellnessRecommendations(
            RiskAssessmentDto risk, UUID userId, UUID sessionId, ActionPriority priority) {
        
        List<ActionRecommendation> recs = new ArrayList<>();
        
        // 1. Continue monitoring
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.CONTINUE_MONITORING)
            .priority(ActionPriority.LOW)
            .title("Continue Wellness Check-ins")
            .description("Great to see you're doing well! Keep monitoring your mental state.")
            .specificAction("Check in with yourself daily. Notice how you're feeling and what affects your mood.")
            .build());
        
        // 2. Meditation
        recs.add(ActionRecommendation.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(risk.getId())
            .actionType(ActionType.PRACTICE_MEDITATION)
            .priority(ActionPriority.LOW)
            .title("Practice Mindfulness Meditation")
            .description("Regular meditation strengthens mental resilience.")
            .specificAction("Try a 5-10 minute guided meditation. Great for maintaining wellness.")
            .resourceType("MEDITATION")
            .resourceUrl("/meditations")
            .build());
        
        return recs;
    }
    
    /**
     * Generate recommendations based on specific risk factors.
     */
    private List<ActionRecommendation> generateFactorSpecificRecommendations(
            RiskAssessmentDto risk, UUID userId, UUID sessionId, ActionPriority priority) {
        
        List<ActionRecommendation> recs = new ArrayList<>();
        Map<String, Integer> factors = risk.getRiskFactors();
        
        // Anxiety/panic
        if (factors.containsKey("panic_symptoms")) {
            recs.add(ActionRecommendation.builder()
                .userId(userId)
                .sessionId(sessionId)
                .riskAssessmentId(risk.getId())
                .actionType(ActionType.DO_BREATHING_EXERCISE)
                .priority(priority)
                .title("Manage Panic with Breathing")
                .description("Breathing exercises are proven to reduce panic attacks.")
                .specificAction("When you feel panic coming: exhale fully, then breathe in slowly for 4 counts, " +
                    "hold for 4, exhale for 6. The longer exhale activates your calm response.")
                .resourceType("EXERCISE")
                .build());
        }
        
        // Isolation
        if (factors.containsKey("severe_isolation") || factors.containsKey("social_withdrawal")) {
            recs.add(ActionRecommendation.builder()
                .userId(userId)
                .sessionId(sessionId)
                .riskAssessmentId(risk.getId())
                .actionType(ActionType.REACH_OUT_TO_FRIEND)
                .priority(priority)
                .title("Break the Isolation")
                .description("Social connection is essential for mental health.")
                .specificAction("Reach out to just one person today. A text, call, or brief meetup. " +
                    "Small steps count.")
                .build());
        }
        
        // Sleep problems
        if (factors.containsKey("insomnia")) {
            recs.add(ActionRecommendation.builder()
                .userId(userId)
                .sessionId(sessionId)
                .riskAssessmentId(risk.getId())
                .actionType(ActionType.IMPROVE_SLEEP_HYGIENE)
                .priority(ActionPriority.MEDIUM)
                .title("Improve Your Sleep")
                .description("Better sleep dramatically improves mental health.")
                .specificAction("Sleep hygiene tips:\n" +
                    "• Same bedtime/wake time daily\n" +
                    "• No screens 1 hour before bed\n" +
                    "• Dark, cool room\n" +
                    "• No caffeine after 2pm\n" +
                    "• Relaxation routine before bed")
                .build());
        }
        
        // Substance use
        if (factors.containsKey("substance_abuse")) {
            recs.add(ActionRecommendation.builder()
                .userId(userId)
                .sessionId(sessionId)
                .riskAssessmentId(risk.getId())
                .actionType(ActionType.REDUCE_SUBSTANCE_USE)
                .priority(ActionPriority.HIGH)
                .title("Address Substance Use")
                .description("Substances often worsen mental health symptoms.")
                .specificAction("Consider speaking with a counsellor about substance use. " +
                    "They can provide support without judgment.")
                .build());
        }
        
        return recs;
    }
    
    /**
     * Get active recommendations for a user.
     */
    @Transactional(readOnly = true)
    public List<ActionRecommendationDto> getActiveRecommendations(UUID userId) {
        return actionRecommendationRepository.findActiveRecommendations(userId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all recommendations for a user.
     */
    @Transactional(readOnly = true)
    public List<ActionRecommendationDto> getUserRecommendations(UUID userId) {
        return actionRecommendationRepository.findByUserIdOrderByPriorityAscCreatedAtDesc(userId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get recommendations for a session.
     */
    @Transactional(readOnly = true)
    public List<ActionRecommendationDto> getSessionRecommendations(UUID sessionId) {
        return actionRecommendationRepository.findBySessionIdOrderByPriorityAscCreatedAtDesc(sessionId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Update recommendation status.
     */
    public ActionRecommendationDto updateStatus(UUID recommendationId, ActionStatus newStatus) {
        ActionRecommendation rec = actionRecommendationRepository.findById(recommendationId)
            .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));
        
        switch (newStatus) {
            case VIEWED -> rec.markViewed();
            case STARTED -> rec.markStarted();
            case COMPLETED -> rec.markCompleted();
            case DISMISSED -> rec.markDismissed();
        }
        
        rec = actionRecommendationRepository.save(rec);
        log.info("Recommendation {} status updated to {}", recommendationId, newStatus);
        
        return toDto(rec);
    }
    
    // ── DTO Conversion ─────────────────────────────────────────────────────────
    
    private ActionRecommendationDto toDto(ActionRecommendation rec) {
        return ActionRecommendationDto.builder()
            .id(rec.getId())
            .userId(rec.getUserId())
            .sessionId(rec.getSessionId())
            .riskAssessmentId(rec.getRiskAssessmentId())
            .actionType(rec.getActionType())
            .priority(rec.getPriority())
            .title(rec.getTitle())
            .description(rec.getDescription())
            .specificAction(rec.getSpecificAction())
            .resourceType(rec.getResourceType())
            .resourceId(rec.getResourceId())
            .resourceUrl(rec.getResourceUrl())
            .status(rec.getStatus())
            .viewedAt(rec.getViewedAt())
            .startedAt(rec.getStartedAt())
            .completedAt(rec.getCompletedAt())
            .dismissedAt(rec.getDismissedAt())
            .expiresAt(rec.getExpiresAt())
            .createdAt(rec.getCreatedAt())
            .updatedAt(rec.getUpdatedAt())
            .isExpired(rec.isExpired())
            .isCritical(rec.isCritical())
            .isUrgent(rec.getPriority().isUrgent())
            .build();
    }
}
