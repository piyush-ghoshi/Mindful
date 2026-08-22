package com.mindful.wellness.entity;

/**
 * Enum for recommended actions based on risk assessment.
 */
public enum RecommendedAction {
    
    /** Crisis intervention needed immediately */
    CRISIS_INTERVENTION("Crisis Intervention", "Immediate crisis support and safety measures"),
    
    /** Alert on-call counsellor urgently */
    URGENT_COUNSELLOR_ALERT("Urgent Counsellor Alert", "Notify on-call counsellor immediately"),
    
    /** Book same-day appointment */
    SAME_DAY_APPOINTMENT("Same-Day Appointment", "Schedule appointment within 24 hours"),
    
    /** Book appointment soon (within 2-3 days) */
    BOOK_APPOINTMENT_SOON("Book Appointment Soon", "Schedule appointment within 2-3 days"),
    
    /** Book appointment when convenient (within week) */
    BOOK_APPOINTMENT_LATER("Book Appointment Later", "Schedule appointment within the week"),
    
    /** Continue self-care and monitoring */
    CONTINUE_MONITORING("Continue Monitoring", "Self-care activities and mood tracking"),
    
    /** Preventive wellness activities */
    PREVENTIVE_CARE("Preventive Care", "Maintain wellness through healthy habits");
    
    private final String displayName;
    private final String description;
    
    RecommendedAction(String displayName, String description) {
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
     * Check if this action requires urgent response.
     */
    public boolean isUrgent() {
        return this == CRISIS_INTERVENTION || 
               this == URGENT_COUNSELLOR_ALERT || 
               this == SAME_DAY_APPOINTMENT;
    }
}
