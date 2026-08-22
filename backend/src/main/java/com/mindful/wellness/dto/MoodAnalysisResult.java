package com.mindful.wellness.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for LLM-based mood analysis result.
 * Represents the structured JSON response from Groq/OpenAI when analyzing mood.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodAnalysisResult {
    
    /**
     * Primary mood detected by AI.
     * One of: SAD, HAPPY, ANXIOUS, ANGRY, OVERWHELMED, HOPELESS, CALM, NEUTRAL
     */
    private String mood;
    
    /**
     * Intensity of the detected mood (0.0 to 1.0).
     * 0.0 = barely detectable, 1.0 = very strong
     */
    @JsonProperty("moodIntensity")
    private BigDecimal moodIntensity;
    
    /**
     * Overall sentiment score (-1.0 to 1.0).
     * -1.0 = very negative, 0.0 = neutral, 1.0 = very positive
     */
    @JsonProperty("sentimentScore")
    private BigDecimal sentimentScore;
    
    /**
     * List of dominant emotions detected.
     * Example: ["sadness", "fatigue", "worry"]
     */
    @JsonProperty("dominantEmotions")
    private List<String> dominantEmotions;
    
    /**
     * Risk indicators detected in the message.
     * Example: ["hopelessness", "isolation", "suicidal_ideation"]
     */
    @JsonProperty("riskIndicators")
    private List<String> riskIndicators;
    
    /**
     * AI confidence in this analysis (0.0 to 1.0).
     * Higher = more confident
     */
    private BigDecimal confidence;
    
    /**
     * Optional: Brief rationale for the mood assessment
     */
    private String rationale;
}
