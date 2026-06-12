package com.mindful.wellness.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessageDto {
    private UUID id;
    private UUID sessionId;
    private String role;       // USER | BOT
    private String content;
    private String severityFlag;
    private LocalDateTime createdAt;
}
