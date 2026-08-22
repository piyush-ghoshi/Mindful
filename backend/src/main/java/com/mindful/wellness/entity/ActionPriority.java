package com.mindful.wellness.entity;

/**
 * Priority levels for action recommendations.
 */
public enum ActionPriority {
    
    /** Critical - execute within minutes (crisis) */
    CRITICAL("Critical", "Immediate action required", 0),
    
    /** High - execute within hours (urgent) */
    HIGH("High", "Action needed soon", 1),
    
    /** Medium - execute within days (important) */
    MEDIUM("Medium", "Recommended action", 2),
    
    /** Low - execute when convenient (optional) */
    LOW("Low", "Optional suggestion", 3);
    
    private final String displayName;
    private final String description;
    private final int sortOrder;
    
    ActionPriority(String displayName, String description, int sortOrder) {
        this.displayName = displayName;
        this.description = description;
        this.sortOrder = sortOrder;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getSortOrder() {
        return sortOrder;
    }
    
    /**
     * Get priority based on risk score.
     */
    public static ActionPriority fromRiskScore(int riskScore) {
        if (riskScore >= 9) return CRITICAL;
        if (riskScore >= 7) return HIGH;
        if (riskScore >= 4) return MEDIUM;
        return LOW;
    }
    
    /**
     * Check if this priority requires urgent attention.
     */
    public boolean isUrgent() {
        return this == CRITICAL || this == HIGH;
    }
}
