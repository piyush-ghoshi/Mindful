package com.mindful.wellness.controller;

import com.mindful.wellness.dto.ConversationDto;
import com.mindful.wellness.dto.MessageDto;
import com.mindful.wellness.dto.SendMessageRequest;
import com.mindful.wellness.service.MessagingService;
import com.mindful.wellness.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for private messaging between students and counsellors.
 *
 * Endpoints:
 *   GET  /api/messages/conversations          — list conversations for current user
 *   POST /api/messages                        — send a message
 *   GET  /api/messages/conversations/{id}     — get messages in a conversation
 *   DELETE /api/messages/{messageId}          — soft-delete a message
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessagingController {

    private final MessagingService messagingService;
    private final AuthUtil authUtil;

    /** List all conversations for the authenticated user. */
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationDto>> getConversations() {
        UUID userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(messagingService.getConversations(userId));
    }

    /** Send a message (creates a conversation if needed). */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        try {
            UUID senderId = authUtil.getCurrentUserId();
            MessageDto message = messagingService.sendMessage(
                    senderId, request.getReceiverId(), request.getContent(), request.getMessageType());
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Get paginated messages in a conversation. */
    @GetMapping("/conversations/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            UUID userId = authUtil.getCurrentUserId();
            Pageable pageable = PageRequest.of(page, Math.min(size, 100));
            Page<MessageDto> messages = messagingService.getMessages(conversationId, userId, pageable);
            return ResponseEntity.ok(Map.of(
                    "content", messages.getContent(),
                    "totalElements", messages.getTotalElements(),
                    "totalPages", messages.getTotalPages(),
                    "currentPage", messages.getNumber()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Soft-delete a message (sender only). */
    @DeleteMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteMessage(@PathVariable UUID messageId) {
        try {
            UUID userId = authUtil.getCurrentUserId();
            messagingService.deleteMessage(messageId, userId);
            return ResponseEntity.ok(Map.of("message", "Message deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
