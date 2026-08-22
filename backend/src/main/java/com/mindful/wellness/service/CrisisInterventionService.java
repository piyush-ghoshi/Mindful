package com.mindful.wellness.service;

import com.mindful.wellness.dto.RiskAssessmentDto;
import com.mindful.wellness.entity.CrisisHotline;
import com.mindful.wellness.entity.CrisisIntervention;
import com.mindful.wellness.entity.EmergencyContact;
import com.mindful.wellness.repository.CrisisHotlineRepository;
import com.mindful.wellness.repository.CrisisInterventionRepository;
import com.mindful.wellness.repository.EmergencyContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for crisis intervention and emergency response.
 * Triggers when user is at high risk of self-harm or suicide.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrisisInterventionService {
    
    private final CrisisInterventionRepository crisisInterventionRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final CrisisHotlineRepository crisisHotlineRepository;
    
    /**
     * Initiate crisis intervention based on risk assessment.
     * Called automatically when risk score >= 9 (crisis level).
     */
    public CrisisIntervention initiateCrisisIntervention(
            RiskAssessmentDto riskAssessment,
            UUID userId,
            UUID sessionId,
            UUID messageId) {
        
        log.warn("🚨 CRISIS INTERVENTION INITIATED for user {} - Risk: {}/10",
            userId, riskAssessment.getTotalRiskScore());
        
        // Determine crisis type from risk factors
        String crisisType = determineCrisisType(riskAssessment);
        
        // Determine intervention type
        String interventionType = determineInterventionType(riskAssessment);
        
        // Create crisis intervention record
        CrisisIntervention intervention = CrisisIntervention.builder()
            .userId(userId)
            .sessionId(sessionId)
            .riskAssessmentId(riskAssessment.getId())
            .messageId(messageId)
            .crisisType(crisisType)
            .severityLevel(riskAssessment.getTotalRiskScore())
            .triggerReason(buildTriggerReason(riskAssessment))
            .interventionType(interventionType)
            .status("INITIATED")
            .helplineProvided(true) // Always provide helpline info
            .followUpRequired(true)
            .followUpDate(LocalDateTime.now().plusHours(24)) // Follow up in 24 hours
            .build();
        
        intervention = crisisInterventionRepository.save(intervention);
        
        // Execute intervention actions
        executeInterventionActions(intervention, riskAssessment, userId);
        
        log.info("Crisis intervention {} created for user {} - Type: {}, Severity: {}/10",
            intervention.getId(), userId, crisisType, riskAssessment.getTotalRiskScore());
        
        return intervention;
    }
    
    /**
     * Determine crisis type from risk factors.
     */
    private String determineCrisisType(RiskAssessmentDto risk) {
        if (risk.getRiskFactors().containsKey("suicidal_ideation") ||
            risk.getRiskFactors().containsKey("suicide_plan") ||
            risk.getRiskFactors().containsKey("suicide_intent")) {
            return "SUICIDAL_IDEATION";
        }
        
        if (risk.getRiskFactors().containsKey("self_harm_intent") ||
            risk.getRiskFactors().containsKey("active_self_harm")) {
            return "SELF_HARM";
        }
        
        if (risk.getRiskFactors().containsKey("substance_abuse")) {
            return "SUBSTANCE_CRISIS";
        }
        
        return "SEVERE_DISTRESS";
    }
    
    /**
     * Determine intervention type based on severity and factors.
     */
    private String determineInterventionType(RiskAssessmentDto risk) {
        // Suicide plan or intent -> immediate emergency response
        if (risk.getRiskFactors().containsKey("suicide_plan") ||
            risk.getRiskFactors().containsKey("suicide_intent")) {
            return "EMERGENCY_CONTACT";
        }
        
        // Very high risk -> alert counsellor + provide helpline
        if (risk.getTotalRiskScore() >= 10) {
            return "COUNSELLOR_ALERT";
        }
        
        // Crisis level (9+) -> auto-provide helpline + create safety plan
        return "AUTO_HELPLINE";
    }
    
    /**
     * Build trigger reason explanation.
     */
    private String buildTriggerReason(RiskAssessmentDto risk) {
        StringBuilder reason = new StringBuilder();
        reason.append("Risk score: ").append(risk.getTotalRiskScore()).append("/10. ");
        reason.append("Risk level: ").append(risk.getRiskLevel()).append(". ");
        
        if (!risk.getRiskFactors().isEmpty()) {
            reason.append("Detected factors: ");
            reason.append(String.join(", ", risk.getRiskFactors().keySet()));
            reason.append(". ");
        }
        
        if (risk.getRationale() != null) {
            reason.append(risk.getRationale());
        }
        
        return reason.toString();
    }
    
    /**
     * Execute intervention actions based on type.
     */
    private void executeInterventionActions(
            CrisisIntervention intervention,
            RiskAssessmentDto risk,
            UUID userId) {
        
        String type = intervention.getInterventionType();
        
        switch (type) {
            case "EMERGENCY_CONTACT" -> {
                // Attempt to contact emergency contacts
                boolean contacted = attemptEmergencyContact(userId, intervention);
                intervention.setEmergencyContacted(contacted);
                
                // Also alert counsellor
                alertCounsellor(userId, intervention);
                intervention.setCounsellorAlerted(true);
            }
            
            case "COUNSELLOR_ALERT" -> {
                // Alert on-call counsellor
                alertCounsellor(userId, intervention);
                intervention.setCounsellorAlerted(true);
            }
            
            case "AUTO_HELPLINE" -> {
                // Helpline info already provided in chat response
                // Mark as provided
                intervention.setHelplineProvided(true);
            }
        }
        
        crisisInterventionRepository.save(intervention);
    }
    
    /**
     * Attempt to contact user's emergency contacts.
     */
    private boolean attemptEmergencyContact(UUID userId, CrisisIntervention intervention) {
        List<EmergencyContact> contacts = 
            emergencyContactRepository.findByUserIdAndCanContactCrisisTrueOrderByPriorityOrderAsc(userId);
        
        if (contacts.isEmpty()) {
            log.warn("No emergency contacts available for user {} during crisis", userId);
            return false;
        }
        
        // In a real system, this would trigger actual SMS/phone call/email
        // For now, we log the attempt
        for (EmergencyContact contact : contacts) {
            log.warn("🚨 CRISIS ALERT: Would contact {} ({}) at {} for user {}",
                contact.getContactName(),
                contact.getRelationship(),
                contact.getPhoneNumber(),
                userId);
            
            // TODO: Integrate with SMS/email service
            // - Send SMS to contact.getPhoneNumber()
            // - Send email to contact.getEmail()
            // - Include crisis details and user's urgent need for support
        }
        
        return true;
    }
    
    /**
     * Alert on-call counsellor about crisis.
     */
    private void alertCounsellor(UUID userId, CrisisIntervention intervention) {
        log.warn("🚨 COUNSELLOR ALERT: User {} in crisis - Severity {}/10 - Intervention ID: {}",
            userId, intervention.getSeverityLevel(), intervention.getId());
        
        // TODO: In real system:
        // - Send push notification to on-call counsellor app
        // - Send email alert with user details and crisis info
        // - Create urgent appointment request
        // - Add to counsellor's priority dashboard
    }
    
    /**
     * Get crisis hotlines for a country.
     */
    @Transactional(readOnly = true)
    public List<CrisisHotline> getCrisisHotlines(String countryCode) {
        return crisisHotlineRepository.findByCountryCodeAndIsActiveTrueOrderByDisplayOrderAsc(countryCode);
    }
    
    /**
     * Get crisis hotlines (default: India).
     */
    @Transactional(readOnly = true)
    public List<CrisisHotline> getCrisisHotlines() {
        return getCrisisHotlines("IN");
    }
    
    /**
     * Get user's crisis intervention history.
     */
    @Transactional(readOnly = true)
    public List<CrisisIntervention> getUserCrisisHistory(UUID userId) {
        return crisisInterventionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get active crisis interventions for user.
     */
    @Transactional(readOnly = true)
    public List<CrisisIntervention> getActiveInterventions(UUID userId) {
        return crisisInterventionRepository.findActiveInterventions(userId);
    }
    
    /**
     * Check if user is currently in active crisis.
     */
    @Transactional(readOnly = true)
    public boolean isUserInCrisis(UUID userId) {
        List<CrisisIntervention> active = crisisInterventionRepository.findActiveInterventions(userId);
        return !active.isEmpty();
    }
    
    /**
     * Resolve a crisis intervention.
     */
    public CrisisIntervention resolveIntervention(UUID interventionId, String resolutionNotes) {
        CrisisIntervention intervention = crisisInterventionRepository.findById(interventionId)
            .orElseThrow(() -> new IllegalArgumentException("Crisis intervention not found"));
        
        intervention.markResolved(resolutionNotes);
        intervention = crisisInterventionRepository.save(intervention);
        
        log.info("Crisis intervention {} resolved: {}", interventionId, resolutionNotes);
        
        return intervention;
    }
    
    /**
     * Check recent crisis activity for a user.
     */
    @Transactional(readOnly = true)
    public boolean hasRecentCrisis(UUID userId, int hoursAgo) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursAgo);
        long count = crisisInterventionRepository.countByUserIdAndCreatedAtAfter(userId, since);
        return count > 0;
    }
}
