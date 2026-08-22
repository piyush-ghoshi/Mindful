package com.mindful.wellness.entity;

/**
 * Enum representing different types of moods that can be detected in user messages.
 * Used by MoodAssessment to categorize emotional states.
 */
public enum MoodType {
    
    /** Positive, joyful emotional state */
    HAPPY("Happy", "Positive, joyful state"),
    
    /** Sad, low, or depressed emotional state */
    SAD("Sad", "Low mood, sadness, depression"),
    
    /** Anxious, worried, or nervous emotional state */
    ANXIOUS("Anxious", "Worry, nervousness, panic"),
    
    /** Angry or irritated emotional state */
    ANGRY("Angry", "Anger, frustration, irritation"),
    
    /** Frustrated or stuck emotional state */
    FRUSTRATED("Frustrated", "Feeling stuck or frustrated"),
    
    /** Hopeful, optimistic emotional state */
    HOPEFUL("Hopeful", "Optimistic, looking forward"),
    
    /** Hopeless or despairing emotional state */
    HOPELESS("Hopeless", "Despair, no hope for future"),
    
    /** Calm, peaceful emotional state */
    CALM("Calm", "Peaceful, relaxed state"),
    
    /** Overwhelmed or stressed emotional state */
    OVERWHELMED("Overwhelmed", "Too much to handle, stressed"),
    
    /** Neutral or unclear emotional state */
    NEUTRAL("Neutral", "No strong emotion detected");
    
    private final String displayName;
    private final String description;
    
    MoodType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this mood type is considered negative/concerning.
     */
    public boolean isNegative() {
        return this == SAD || this == ANXIOUS || this == ANGRY || 
               this == FRUSTRATED || this == HOPELESS || this == OVERWHELMED;
    }
    
    /**
     * Check if this mood type is considered positive/healthy.
     */
    public boolean isPositive() {
        return this == HAPPY || this == HOPEFUL || this == CALM;
    }
    
    /**
     * Get severity weight for this mood (1-5, higher = more concerning).
     */
    public int getSeverityWeight() {
        return switch (this) {
            case HOPELESS -> 5;
            case ANXIOUS, OVERWHELMED -> 4;
            case SAD, ANGRY -> 3;
            case FRUSTRATED -> 2;
            case NEUTRAL, CALM -> 1;
            case HOPEFUL, HAPPY -> 0;
        };
    }
}
