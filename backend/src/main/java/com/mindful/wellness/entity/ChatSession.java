package com.mindful.wellness.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single MindBot session for a user.
 * Each session has a type (ASSESSMENT = report-generating, CASUAL = open chat)
 * and tracks message count and whether a report has been generated.
 */
@Entity
@Table(name = "chat_sessions", indexes = {
    @Index(name = "idx_chat_sessions_user_id", columnList = "user_id"),
    @Index(name = "idx_chat_sessions_created_at", columnList = "created_at")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** ASSESSMENT or CASUAL */
    @Column(name = "session_type", nullable = false, length = 20)
    private String sessionType;

    /** How many user messages sent this session */
    @Column(name = "message_count", nullable = false)
    private Integer messageCount = 0;

    /** Whether a report was generated for this session */
    @Column(name = "report_generated", nullable = false)
    private Boolean reportGenerated = false;

    /** Severity detected: LOW / MODERATE / HIGH / SEVERE */
    @Column(name = "detected_severity", length = 20)
    private String detectedSeverity;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
