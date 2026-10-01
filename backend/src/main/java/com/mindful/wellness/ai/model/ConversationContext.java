package com.mindful.wellness.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Structured conversation context provided to AI providers.
 * Encapsulates user identity, session state, mood history, and recent dialogue turns.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationContext {
    private UUID userId;
    private String userName;
    private UUID sessionId;
    private String sessionType; // "CASUAL" or "ASSESSMENT"
    private String languagePreference;

    @Builder.Default
    private List<LlmMessage> history = new ArrayList<>();

    private String latestUserMessage;
    private String currentMood;
    private String detectedSeverity;
    private Integer highestRiskScore;
    private String uploadedReportSummary;
}
