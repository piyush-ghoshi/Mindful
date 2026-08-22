package com.mindful.wellness.controller;

import com.mindful.wellness.entity.CrisisHotline;
import com.mindful.wellness.entity.CrisisIntervention;
import com.mindful.wellness.entity.EmergencyContact;
import com.mindful.wellness.repository.EmergencyContactRepository;
import com.mindful.wellness.service.CrisisInterventionService;
import com.mindful.wellness.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Crisis Intervention & Emergency Response endpoints.
 * 
 * Handles:
 * - Crisis hotline information
 * - Emergency contact management
 * - Crisis intervention history
 */
@RestController
@RequestMapping("/api/crisis")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class CrisisController {
    
    private final CrisisInterventionService crisisInterventionService;
    private final EmergencyContactRepository emergencyContactRepository;
    private final AuthUtil authUtil;
    
    // ── Crisis Hotlines ────────────────────────────────────────────────────────
    
    /**
     * Get crisis hotlines (helpline numbers).
     * PUBLIC endpoint - no auth required for crisis access.
     * 
     * GET /api/crisis/hotlines?country=IN
     */
    @GetMapping("/hotlines")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CrisisHotline>> getCrisisHotlines(
            @RequestParam(defaultValue = "IN") String country) {
        List<CrisisHotline> hotlines = crisisInterventionService.getCrisisHotlines(country);
        return ResponseEntity.ok(hotlines);
    }
    
    // ── Emergency Contacts ─────────────────────────────────────────────────────
    
    /**
     * Get user's emergency contacts.
     * 
     * GET /api/crisis/emergency-contacts
     */
    @GetMapping("/emergency-contacts")
    public ResponseEntity<List<EmergencyContact>> getEmergencyContacts(Authentication auth) {
        UUID userId = authUtil.getUserId(auth);
        List<EmergencyContact> contacts = 
            emergencyContactRepository.findByUserIdOrderByPriorityOrderAsc(userId);
        return ResponseEntity.ok(contacts);
    }
    
    /**
     * Add emergency contact.
     * 
     * POST /api/crisis/emergency-contacts
     */
    @PostMapping("/emergency-contacts")
    public ResponseEntity<?> addEmergencyContact(
            Authentication auth,
            @RequestBody EmergencyContact contact) {
        try {
            UUID userId = authUtil.getUserId(auth);
            contact.setUserId(userId);
            contact.setId(null); // Ensure new record
            
            EmergencyContact saved = emergencyContactRepository.save(contact);
            
            log.info("Emergency contact added for user {}: {} ({})",
                userId, contact.getContactName(), contact.getRelationship());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error adding emergency contact", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to add emergency contact"));
        }
    }
    
    /**
     * Update emergency contact.
     * 
     * PUT /api/crisis/emergency-contacts/{contactId}
     */
    @PutMapping("/emergency-contacts/{contactId}")
    public ResponseEntity<?> updateEmergencyContact(
            Authentication auth,
            @PathVariable UUID contactId,
            @RequestBody EmergencyContact updatedContact) {
        try {
            UUID userId = authUtil.getUserId(auth);
            
            EmergencyContact existing = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
            
            if (!existing.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied"));
            }
            
            // Update fields
            existing.setContactName(updatedContact.getContactName());
            existing.setRelationship(updatedContact.getRelationship());
            existing.setPhoneNumber(updatedContact.getPhoneNumber());
            existing.setEmail(updatedContact.getEmail());
            existing.setPriorityOrder(updatedContact.getPriorityOrder());
            existing.setCanContactCrisis(updatedContact.getCanContactCrisis());
            existing.setPreferredMethod(updatedContact.getPreferredMethod());
            
            EmergencyContact saved = emergencyContactRepository.save(existing);
            
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating emergency contact", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to update emergency contact"));
        }
    }
    
    /**
     * Delete emergency contact.
     * 
     * DELETE /api/crisis/emergency-contacts/{contactId}
     */
    @DeleteMapping("/emergency-contacts/{contactId}")
    public ResponseEntity<?> deleteEmergencyContact(
            Authentication auth,
            @PathVariable UUID contactId) {
        try {
            UUID userId = authUtil.getUserId(auth);
            
            EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
            
            if (!contact.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied"));
            }
            
            emergencyContactRepository.delete(contact);
            
            return ResponseEntity.ok(Map.of("message", "Emergency contact deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting emergency contact", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to delete emergency contact"));
        }
    }
    
    // ── Crisis Intervention History ────────────────────────────────────────────
    
    /**
     * Get user's crisis intervention history.
     * 
     * GET /api/crisis/interventions
     */
    @GetMapping("/interventions")
    public ResponseEntity<List<CrisisIntervention>> getCrisisHistory(Authentication auth) {
        UUID userId = authUtil.getUserId(auth);
        List<CrisisIntervention> interventions = 
            crisisInterventionService.getUserCrisisHistory(userId);
        return ResponseEntity.ok(interventions);
    }
    
    /**
     * Get active crisis interventions for user.
     * 
     * GET /api/crisis/interventions/active
     */
    @GetMapping("/interventions/active")
    public ResponseEntity<?> getActiveInterventions(Authentication auth) {
        UUID userId = authUtil.getUserId(auth);
        List<CrisisIntervention> active = 
            crisisInterventionService.getActiveInterventions(userId);
        
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "activeCount", active.size(),
            "isInCrisis", !active.isEmpty(),
            "interventions", active
        ));
    }
    
    /**
     * Check if user is currently in crisis.
     * 
     * GET /api/crisis/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getCrisisStatus(Authentication auth) {
        UUID userId = authUtil.getUserId(auth);
        boolean inCrisis = crisisInterventionService.isUserInCrisis(userId);
        boolean recentCrisis = crisisInterventionService.hasRecentCrisis(userId, 48);
        
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "currentlyInCrisis", inCrisis,
            "hadRecentCrisis", recentCrisis
        ));
    }
    
    /**
     * Resolve a crisis intervention (ADMIN/COUNSELLOR only).
     * 
     * POST /api/crisis/interventions/{interventionId}/resolve
     */
    @PostMapping("/interventions/{interventionId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELLOR')")
    public ResponseEntity<?> resolveIntervention(
            Authentication auth,
            @PathVariable UUID interventionId,
            @RequestBody Map<String, String> body) {
        try {
            String resolutionNotes = body.get("notes");
            if (resolutionNotes == null || resolutionNotes.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Resolution notes are required"));
            }
            
            CrisisIntervention resolved = 
                crisisInterventionService.resolveIntervention(interventionId, resolutionNotes);
            
            return ResponseEntity.ok(resolved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error resolving crisis intervention", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to resolve intervention"));
        }
    }
}
