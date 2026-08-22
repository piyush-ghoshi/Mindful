package com.mindful.wellness.entity;

/**
 * Status of action recommendation implementation.
 */
public enum ActionStatus {
    
    /** Recommendation created, not yet viewed by user */
    PENDING("Pending", "Not yet viewed"),
    
    /** User has viewed the recommendation */
    VIEWED("Viewed", "Seen by user"),
    
    /** User has started working on the action */
    STARTED("Started", "In progress"),
    
    /** User has completed the action */
    COMPLETED("Completed", "Done"),
    
    /** User has dismissed the recommendation */
    DISMISSED("Dismissed", "User chose not to pursue");
    
    private final String displayName;
    private final String description;
    
    ActionStatus(String displayName, String description) {
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
     * Check if this status indicates the action is still active.
     */
    public boolean isActive() {
        return this == PENDING || this == VIEWED || this == STARTED;
    }
    
    /**
     * Check if this status indicates completion (success or dismissed).
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == DISMISSED;
    }
}
