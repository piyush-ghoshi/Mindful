package com.mindful.wellness.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of real-time safety and crisis evaluation on user messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyCheckResult {
    private boolean isCrisis;
    private String riskCategory; // "SUICIDE_SELF_HARM", "SEVERE_DISTRESS", "SAFE"
    private String emergencyResponse;
    private String triggerPhrase;
    private double riskScore;

    @Builder.Default
    private List<String> flaggedIndicators = new ArrayList<>();

    public static SafetyCheckResult safe() {
        return SafetyCheckResult.builder()
                .isCrisis(false)
                .riskCategory("SAFE")
                .riskScore(0.0)
                .build();
    }

    public static SafetyCheckResult crisis(String category, String response, String triggerPhrase, double score) {
        return SafetyCheckResult.builder()
                .isCrisis(true)
                .riskCategory(category)
                .emergencyResponse(response)
                .triggerPhrase(triggerPhrase)
                .riskScore(score)
                .build();
    }
}
