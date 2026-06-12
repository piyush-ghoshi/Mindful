package com.mindful.wellness.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatSessionDto {
    private UUID id;
    private String sessionType;   // ASSESSMENT | CASUAL
    private Integer messageCount;
    private Boolean reportGenerated;
    private String detectedSeverity;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private List<ChatMessageDto> messages;

    // Rate-limit info for the client
    private int messagesUsedToday;
    private int messagesDailyLimit;
    private int reportsUsedToday;
    private int reportsDailyLimit;
}
