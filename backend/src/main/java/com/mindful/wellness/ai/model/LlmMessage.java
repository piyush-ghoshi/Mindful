package com.mindful.wellness.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Normalized representation of a chat message in the LLM conversation stream.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmMessage {
    private String role; // "system", "user", "assistant"
    private String content;
    private LocalDateTime timestamp;

    public static LlmMessage of(String role, String content) {
        return LlmMessage.builder()
                .role(role)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
