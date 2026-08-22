package com.mindful.wellness.entity;

/**
 * Enum representing risk levels based on total risk score.
 */
public enum RiskLevel {
    
    /** Minimal risk (score 0-2) - general wellness concerns */
    MINIMAL("Minimal", "General wellness concerns, no immediate risk", 0, 2),
    
    /** Low risk (score 3-4) - mild distress */
    LOW("Low", "Mild distress, monitoring recommended", 3, 4),
    
    /** Moderate risk (score 5-6) - significant distress with warning signs */
    MODERATE("Moderate", "Significant distress, intervention recommended", 5, 6),
    
    /** High risk (score 7-8) - severe distress with multiple risk factors */
    HIGH("High", "Severe distress, urgent professional support needed", 7, 8),
    
    /** Severe/Critical risk (score 9-10) - imminent danger */
    SEVERE("Severe", "Critical risk, immediate intervention required", 9, 10);
    
    private final String displayName;
    private final String description;
    private final int minScore;
    private final int maxScore;
    
    RiskLevel(String displayName, String description, int minScore, int maxScore) {
        this.displayName = displayName;
        this.description = description;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getMinScore() {
        return minScore;
    }
    
    public int getMaxScore() {
        return maxScore;
    }
    
    /**
     * Get risk level from score.
     */
    public static RiskLevel fromScore(int score) {
        if (score <= 2) return MINIMAL;
        if (score <= 4) return LOW;
        if (score <= 6) return MODERATE;
        if (score <= 8) return HIGH;
        return SEVERE;
    }
    
    /**
     * Check if this risk level requires immediate attention.
     */
    public boolean requiresImmediateAction() {
        return this == HIGH || this == SEVERE;
    }
    
    /**
     * Check if this risk level requires professional intervention.
     */
    public boolean requiresProfessionalHelp() {
        return this == MODERATE || this == HIGH || this == SEVERE;
    }
}
