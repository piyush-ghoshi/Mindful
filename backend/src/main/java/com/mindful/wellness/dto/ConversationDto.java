package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private UUID id;
    private UUID otherUserId;
    private String otherUserName;
    private String otherUserRole;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
}
