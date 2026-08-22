package com.mindful.wellness.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a mood and sentiment analysis for a single chat message.
 * 
 * Each user message in a chat session gets analyzed for:
 * - Detected mood (happy, sad, anxious, etc.)
 * - Mood intensity (how strong the emotion is)
 * - Sentiment score (overall positivity/negativity)
 * - Analysis method (AI, keyword-based, or hybrid)
 * 
 * This data is used to:
 * - Track emotional state over time
 * - Identify concerning patterns
 * - Generate personalized recommendations
 * - Create session summaries
 */
@Entity
@Table(name = "mood_assessments", indexes = {
    @Index(name = "idx_mood_session", columnList = "session_id"),
    @Index(name = "idx_mood_message", columnList = "message_id"),
    @Index(name = "idx_mood_user", columnList = "user_id"),
    @Index(name = "idx_mood_created_at", columnList = "created_at"),
    @Index(name = "idx_mood_detected", columnList = "detected_mood"),
    @Index(name = "idx_mood_intensity", columnList = "mood_intensity"),
    @Index(name = "idx_mood_user_time", columnList = "user_id, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodAssessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;
    
    @Column(name = "message_id", nullable = false)
    private UUID messageId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    // ── Mood Analysis ──────────────────────────────────────────────────────────
    
    /**
     * Primary mood detected in the message.
     * Examples: HAPPY, SAD, ANXIOUS, ANGRY, etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "detected_mood", nullable = false, length = 50)
    private MoodType detectedMood;
    
    /**
     * Intensity of the detected mood.
     * Range: 0.00 (barely detectable) to 1.00 (very strong)
     * 
     * Examples:
     * - "I'm a bit sad" → 0.4
     * - "I'm extremely devastated" → 0.95
     */
    @Column(name = "mood_intensity", nullable = false, precision = 3, scale = 2)
    private BigDecimal moodIntensity;
    
    /**
     * Overall sentiment score of the message.
     * Range: -1.00 (very negative) to 1.00 (very positive)
     * 
     * Examples:
     * - "I hate everything" → -0.9
     * - "Things are okay" → 0.2
     * - "I'm so happy!" → 0.85
     */
    @Column(name = "sentiment_score", nullable = false, precision = 3, scale = 2)
    private BigDecimal sentimentScore;
    
    /**
     * Secondary emotions detected (JSON array).
     * Example: ["sadness", "fatigue", "worry"]
     * Stored as JSON string for flexibility.
     */
    @Column(name = "dominant_emotions", columnDefinition = "TEXT")
    private String dominantEmotions;
    
    // ── Analysis Metadata ──────────────────────────────────────────────────────
    
    /**
     * Method used to perform the analysis.
     * - AI: LLM-based analysis (Groq, OpenAI)
     * - KEYWORD: Rule-based keyword matching
     * - HYBRID: Both AI and keyword-based
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_method", nullable = false, length = 20)
    @Builder.Default
    private AnalysisMethod analysisMethod = AnalysisMethod.KEYWORD;
    
    /**
     * Confidence score in the analysis (for AI methods).
     * Range: 0.00 (not confident) to 1.00 (very confident)
     * Null for keyword-based analysis.
     */
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;
    
    /**
     * Risk indicators detected in this message (JSON array).
     * Example: ["hopelessness", "isolation", "suicidal_ideation"]
     * Used in Phase 2 for risk assessment.
     */
    @Column(name = "risk_indicators", columnDefinition = "TEXT")
    private String riskIndicators;
    
    // ── Timestamps ─────────────────────────────────────────────────────────────
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // ── Helper Methods ─────────────────────────────────────────────────────────
    
    /**
     * Check if this assessment indicates a concerning mood.
     */
    public boolean isConcerning() {
        return detectedMood.isNegative() && 
               moodIntensity.compareTo(BigDecimal.valueOf(0.6)) > 0;
    }
    
    /**
     * Check if this assessment indicates high intensity emotion.
     */
    public boolean isHighIntensity() {
        return moodIntensity.compareTo(BigDecimal.valueOf(0.7)) >= 0;
    }
    
    /**
     * Check if sentiment is negative.
     */
    public boolean isNegativeSentiment() {
        return sentimentScore.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * Get a human-readable description of this assessment.
     */
    public String getDescription() {
        String intensityDesc = moodIntensity.compareTo(BigDecimal.valueOf(0.7)) >= 0 ? "strong" :
                              moodIntensity.compareTo(BigDecimal.valueOf(0.4)) >= 0 ? "moderate" : "mild";
        
        return String.format("%s %s mood (sentiment: %.2f)", 
            intensityDesc, 
            detectedMood.getDisplayName().toLowerCase(),
            sentimentScore
        );
    }
}
