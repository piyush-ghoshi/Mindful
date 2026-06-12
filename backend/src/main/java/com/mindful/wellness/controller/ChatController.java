package com.mindful.wellness.controller;

import com.mindful.wellness.dto.*;
import com.mindful.wellness.service.ChatService;
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
}
