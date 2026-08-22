package com.mindful.wellness.entity;

/**
 * Types of recommended actions for mental health intervention.
 */
public enum ActionType {
    
    // ── Crisis Response ────────────────────────────────────────────────────────
    CALL_CRISIS_HELPLINE("Call Crisis Helpline", "Immediate crisis support"),
    ALERT_EMERGENCY_CONTACT("Alert Emergency Contact", "Notify trusted person immediately"),
    CALL_COUNSELLOR_NOW("Call On-Call Counsellor", "Speak with counsellor urgently"),
    
    // ── Professional Support ───────────────────────────────────────────────────
    BOOK_URGENT_APPOINTMENT("Book Urgent Appointment", "Schedule within 24-48 hours"),
    BOOK_APPOINTMENT("Book Appointment", "Schedule counselling session"),
    JOIN_SUPPORT_GROUP("Join Support Group", "Connect with peer support"),
    
    // ── Self-Care Activities ───────────────────────────────────────────────────
    DO_BREATHING_EXERCISE("Do Breathing Exercise", "Immediate anxiety relief"),
    DO_GROUNDING_EXERCISE("Do Grounding Exercise", "Return to present moment"),
    DO_PHYSICAL_EXERCISE("Do Physical Exercise", "Movement for mood improvement"),
    PRACTICE_MEDITATION("Practice Meditation", "Guided mindfulness session"),
    JOURNAL_FEELINGS("Journal Your Feelings", "Express emotions through writing"),
    
    // ── Social Support ─────────────────────────────────────────────────────────
    REACH_OUT_TO_FRIEND("Reach Out to Someone", "Connect with trusted person"),
    JOIN_COMMUNITY_ACTIVITY("Join Community Activity", "Social engagement"),
    
    // ── Education & Resources ──────────────────────────────────────────────────
    READ_ARTICLE("Read Article", "Learn coping strategies"),
    WATCH_VIDEO("Watch Video", "Educational mental health content"),
    COMPLETE_WORKSHEET("Complete Worksheet", "Structured self-reflection"),
    
    // ── Wellness Habits ────────────────────────────────────────────────────────
    IMPROVE_SLEEP_HYGIENE("Improve Sleep Hygiene", "Better sleep habits"),
    EAT_NUTRITIOUS_MEAL("Eat Nutritious Meal", "Support physical health"),
    REDUCE_SUBSTANCE_USE("Reduce Substance Use", "Limit alcohol/drugs"),
    SPEND_TIME_OUTDOORS("Spend Time Outdoors", "Nature exposure"),
    
    // ── Safety Planning ────────────────────────────────────────────────────────
    CREATE_SAFETY_PLAN("Create Safety Plan", "Crisis prevention strategy"),
    REMOVE_MEANS_ACCESS("Limit Means Access", "Remove harmful items"),
    
    // ── Monitoring ─────────────────────────────────────────────────────────────
    TRACK_MOOD_DAILY("Track Mood Daily", "Monitor mental state"),
    CONTINUE_MONITORING("Continue Monitoring", "Ongoing wellness check-ins");
    
    private final String displayName;
    private final String shortDescription;
    
    ActionType(String displayName, String shortDescription) {
        this.displayName = displayName;
        this.shortDescription = shortDescription;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getShortDescription() {
        return shortDescription;
    }
    
    /**
     * Check if this action requires immediate execution.
     */
    public boolean isUrgent() {
        return this == CALL_CRISIS_HELPLINE ||
               this == ALERT_EMERGENCY_CONTACT ||
               this == CALL_COUNSELLOR_NOW ||
               this == BOOK_URGENT_APPOINTMENT;
    }
    
    /**
     * Check if this is a crisis-level action.
     */
    public boolean isCrisisAction() {
        return this == CALL_CRISIS_HELPLINE ||
               this == ALERT_EMERGENCY_CONTACT ||
               this == CALL_COUNSELLOR_NOW;
    }
}
