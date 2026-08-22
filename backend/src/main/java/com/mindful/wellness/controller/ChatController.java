package com.mindful.wellness.controller;

import com.mindful.wellness.dto.*;
import com.mindful.wellness.service.ChatService;
import com.mindful.wellness.service.MoodAnalysisService;
import com.mindful.wellness.util.AuthUtil;
import jakarta.validation.Valid;
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
 * MindBot AI Chat endpoints.
 *
 * POST /api/chat/sessions                          — start a new session
 * POST /api/chat/sessions/{id}/messages            — send a message
 * GET  /api/chat/sessions/{id}/messages            — get session messages
 * POST /api/chat/sessions/{id}/report              — generate report
 * GET  /api/chat/reports                           — list user's reports
 * GET  /api/chat/reports/{reportId}                — get specific report
 * GET  /api/chat/rate-limit                        — rate limit info
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatService chatService;
    private final MoodAnalysisService moodAnalysisService;
    private final com.mindful.wellness.service.SeverityAssessmentService severityAssessmentService;
    private final com.mindful.wellness.repository.RiskAssessmentRepository riskAssessmentRepository;
    private final com.mindful.wellness.service.ActionRecommendationService actionRecommendationService;
    private final AuthUtil authUtil;

    /** Start a new session — type = ASSESSMENT or CASUAL */
    @PostMapping("/sessions")
    public ResponseEntity<?> startSession(
            Authentication auth,
            @RequestParam(defaultValue = "CASUAL") String type) {
        try {
            UUID userId = authUtil.getUserId(auth);
            ChatSessionDto session = chatService.startSession(userId, type);
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error starting chat session", e);
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    /** Get the current active session, if one exists */
    @GetMapping("/sessions/active")
    public ResponseEntity<?> getActiveSession(Authentication auth) {
        try {
            UUID userId = authUtil.getUserId(auth);
            return chatService.getActiveSession(userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
        } catch (Exception e) {
            log.error("Error getting active chat session", e);
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    /** End the current active session */
    @PostMapping("/sessions/active/end")
    public ResponseEntity<?> endActiveSession(Authentication auth) {
        try {
            UUID userId = authUtil.getUserId(auth);
            chatService.endActiveSession(userId);
            return ResponseEntity.ok(Map.of("message", "Active session ended successfully"));
        } catch (Exception e) {
            log.error("Error ending active session", e);
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    /** Send a message in a session */
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<?> sendMessage(
            Authentication auth,
            @PathVariable UUID sessionId,
            @Valid @RequestBody SendChatMessageRequest request) {
        try {
            UUID userId = authUtil.getUserId(auth);
            ChatMessageDto reply = chatService.sendMessage(
                    userId, sessionId, request.getContent(), request.getUploadedReportText());
            return ResponseEntity.ok(reply);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error sending chat message", e);
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    /** Get all messages for a session */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<?> getMessages(Authentication auth, @PathVariable UUID sessionId) {
        try {
            UUID userId = authUtil.getUserId(auth);
            List<ChatMessageDto> messages = chatService.getSessionMessages(userId, sessionId);
            return ResponseEntity.ok(messages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Generate a mental health report for a completed session */
    @PostMapping("/sessions/{sessionId}/report")
    public ResponseEntity<?> generateReport(Authentication auth, @PathVariable UUID sessionId) {
        try {
            UUID userId = authUtil.getUserId(auth);
            MentalHealthReportDto report = chatService.generateReport(userId, sessionId);
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error generating report", e);
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    /** List all reports for the current user */
    @GetMapping("/reports")
    public ResponseEntity<List<MentalHealthReportDto>> getUserReports(Authentication auth) {
        UUID userId = authUtil.getUserId(auth);
        return ResponseEntity.ok(chatService.getUserReports(userId));
    }

    /** Get a specific report */
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<?> getReport(Authentication auth, @PathVariable UUID reportId) {
        try {
            UUID userId = authUtil.getUserId(auth);
            return ResponseEntity.ok(chatService.getReport(userId, reportId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Rate limit info for the current user */
    @GetMapping("/rate-limit")
    public ResponseEntity<Map<String, Object>> getRateLimitInfo(Authentication auth) {
        UUID userId = authUtil.getUserId(auth);
        return ResponseEntity.ok(chatService.getRateLimitInfo(userId));
    }
    
    // ── Mood Analysis Endpoints ────────────────────────────────────────────────
    
    /**
     * Get mood analysis trajectory for a session.
     * Shows how mood changes over the conversation.
     * 
     * GET /api/chat/sessions/{sessionId}/mood-analysis
     */
    @GetMapping("/sessions/{sessionId}/mood-analysis")
    public ResponseEntity<?> getSessionMoodAnalysis(
            Authentication auth,
            @PathVariable UUID sessionId) {
        try {
            UUID userId = authUtil.getUserId(auth);
            // TODO: Add ownership check
            MoodTrajectoryDto trajectory = moodAnalysisService.getSessionMoodTrajectory(sessionId);
            return ResponseEntity.ok(trajectory);
        } catch (Exception e) {
            log.error("Error getting mood analysis", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to retrieve mood analysis"));
        }
    }
    
    /**
     * Get significant mood shifts detected in a session.
     * Identifies concerning changes or improvements.
     * 
     * GET /api/chat/sessions/{sessionId}/mood-shifts
     */
    @GetMapping("/sessions/{sessionId}/mood-shifts")
    public ResponseEntity<?> getSessionMoodShifts(
            Authentication auth,
            @PathVariable UUID sessionId) {
        try {
            UUID userId = authUtil.getUserId(auth);
            // TODO: Add ownership check
            List<MoodShiftDto> shifts = moodAnalysisService.detectMoodShifts(sessionId);
            return ResponseEntity.ok(shifts);
        } catch (Exception e) {
            log.error("Error getting mood shifts", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to retrieve mood shifts"));
        }
    }
    
    // ── Risk Assessment Endpoints ──────────────────────────────────────────────
    
    /**
     * Get all risk assessments for a session.
     * Shows risk level progression throughout the conversation.
     * 
     * GET /api/chat/sessions/{sessionId}/risk-analysis
     */
    @GetMapping("/sessions/{sessionId}/risk-analysis")
    public ResponseEntity<?> getSessionRiskAnalysis(
            Authentication auth,
            @PathVariable UUID sessionId) {
        try {
            UUID userId = authUtil.getUserId(auth);
            // TODO: Add ownership check
            
            List<com.mindful.wellness.entity.RiskAssessment> assessments = 
                riskAssessmentRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
            
            List<com.mindful.wellness.dto.RiskAssessmentDto> dtos = assessments.stream()
                .map(this::toRiskDto)
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "assessmentCount", dtos.size(),
                "highestRiskScore", assessments.stream()
                    .mapToInt(com.mindful.wellness.entity.RiskAssessment::getTotalRiskScore)
                    .max()
                    .orElse(0),
                "assessments", dtos
            ));
        } catch (Exception e) {
            log.error("Error getting risk analysis", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to retrieve risk analysis"));
        }
    }
    
    /**
     * Get risk trend for the authenticated user.
     * Analyzes if risk is improving, worsening, or stable.
     * 
     * GET /api/chat/risk-trend?days=7
     */
    @GetMapping("/risk-trend")
    public ResponseEntity<?> getRiskTrend(
            Authentication auth,
            @RequestParam(defaultValue = "7") int days) {
        try {
            UUID userId = authUtil.getUserId(auth);
            java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(days);
            
            String trend = severityAssessmentService.getRiskTrend(userId, since);
            
            List<com.mindful.wellness.entity.RiskAssessment> recentAssessments = 
                riskAssessmentRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
            
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "period", days + " days",
                "trend", trend,
                "recentAssessments", recentAssessments.stream()
                    .map(this::toRiskDto)
                    .collect(java.util.stream.Collectors.toList())
            ));
        } catch (Exception e) {
            log.error("Error getting risk trend", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to retrieve risk trend"));
        }
    }
    
    /**
     * Get high-risk assessments for the authenticated user.
     * Returns all assessments with risk score >= 7.
     * 
     * GET /api/chat/risk-alerts
     */
    @GetMapping("/risk-alerts")
    public ResponseEntity<?> getRiskAlerts(Authentication auth) {
        try {
            UUID userId = authUtil.getUserId(auth);
            
            List<com.mindful.wellness.entity.RiskAssessment> highRisk = 
                riskAssessmentRepository.findHighRiskAssessments(userId);
            
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "alertCount", highRisk.size(),
                "alerts", highRisk.stream()
                    .map(this::toRiskDto)
                    .collect(java.util.stream.Collectors.toList())
            ));
        } catch (Exception e) {
            log.error("Error getting risk alerts", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to retrieve risk alerts"));
        }
    }
    
    // ── Action Recommendation Endpoints ────────────────────────────────────────
    
    /**
     * Get active action recommendations for the authenticated user.
     * Returns pending, viewed, and started recommendations.
     * 
     * GET /api/chat/recommendations/active
     */
    @GetMapping("/recommendations/active")
    public ResponseEntity<?> getActiveRecommendations(Authentication auth) {
        try {
            UUID userId = authUtil.getUserId(auth);
            List<com.mindful.wellness.dto.ActionRecommendationDto> recs = 
                actionRecommendationService.getActiveRecommendations(userId);
            
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "activeCount", recs.size(),
                "recommendations", recs
            ));
        } catch (Exception e) {
            log.error("Error getting active recommendations", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to retrieve recommendations"));
        }
    }
    
    /**
     * Get all action recommendations for the authenticated user.
     * 
     * GET /api/chat/recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<?> getUserRecommendations(Authentication auth) {
        try {
            UUID userId = authUtil.getUserId(auth);
            List<com.mindful.wellness.dto.ActionRecommendationDto> recs = 
                actionRecommendationService.getUserRecommendations(userId);
            
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "totalCount", recs.size(),
                "recommendations", recs
            ));
        } catch (Exception e) {
            log.error("Error getting user recommendations", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to retrieve recommendations"));
        }
    }
    
    /**
     * Get recommendations for a specific session.
     * 
     * GET /api/chat/sessions/{sessionId}/recommendations
     */
    @GetMapping("/sessions/{sessionId}/recommendations")
    public ResponseEntity<?> getSessionRecommendations(
            Authentication auth,
            @PathVariable UUID sessionId) {
        try {
            UUID userId = authUtil.getUserId(auth);
            // TODO: Add ownership check
            
            List<com.mindful.wellness.dto.ActionRecommendationDto> recs = 
                actionRecommendationService.getSessionRecommendations(sessionId);
            
            return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "recommendationCount", recs.size(),
                "recommendations", recs
            ));
        } catch (Exception e) {
            log.error("Error getting session recommendations", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to retrieve session recommendations"));
        }
    }
    
    /**
     * Update the status of an action recommendation.
     * 
     * PATCH /api/chat/recommendations/{recommendationId}/status
     * Body: { "status": "VIEWED" | "STARTED" | "COMPLETED" | "DISMISSED" }
     */
    @PatchMapping("/recommendations/{recommendationId}/status")
    public ResponseEntity<?> updateRecommendationStatus(
            Authentication auth,
            @PathVariable UUID recommendationId,
            @RequestBody Map<String, String> body) {
        try {
            UUID userId = authUtil.getUserId(auth);
            
            String statusStr = body.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Status is required"));
            }
            
            com.mindful.wellness.entity.ActionStatus status;
            try {
                status = com.mindful.wellness.entity.ActionStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid status: " + statusStr));
            }
            
            com.mindful.wellness.dto.ActionRecommendationDto updated = 
                actionRecommendationService.updateStatus(recommendationId, status);
            
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating recommendation status", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to update recommendation status"));
        }
    }
    
    // ── Helper Methods ─────────────────────────────────────────────────────────
    
    private com.mindful.wellness.dto.RiskAssessmentDto toRiskDto(com.mindful.wellness.entity.RiskAssessment assessment) {
        // Parse JSON fields
        Map<String, Integer> riskFactors = parseJsonMap(assessment.getRiskFactors());
        Map<String, Object> protectiveFactors = parseJsonMapObject(assessment.getProtectiveFactors());
        
        return com.mindful.wellness.dto.RiskAssessmentDto.builder()
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
    
    private Map<String, Integer> parseJsonMap(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("{}")) {
            return Map.of();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON map: {}", e.getMessage());
            return Map.of();
        }
    }
    
    private Map<String, Object> parseJsonMapObject(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("{}")) {
            return Map.of();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON map: {}", e.getMessage());
            return Map.of();
        }
    }
}
