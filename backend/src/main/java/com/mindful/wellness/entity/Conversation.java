package com.mindful.wellness.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Conversation entity representing a private messaging thread between two users.
 *
 * Tracks the two participants, last message metadata, and per-participant unread counts.
 */
@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_conversations_participant1", columnList = "participant1_id"),
        @Index(name = "idx_conversations_participant2", columnList = "participant2_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "participant1_id", nullable = false)
    private UUID participant1Id;

    @NotNull
    @Column(name = "participant2_id", nullable = false)
    private UUID participant2Id;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_message_preview", length = 255)
    private String lastMessagePreview;

    @Column(name = "unread_count1", nullable = false)
    private Integer unreadCount1 = 0;

    @Column(name = "unread_count2", nullable = false)
    private Integer unreadCount2 = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
