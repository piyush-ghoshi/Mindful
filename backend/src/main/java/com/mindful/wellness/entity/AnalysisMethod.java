package com.mindful.wellness.entity;

/**
 * Enum representing the method used for mood/risk analysis.
 * Indicates whether the analysis was performed by AI, keywords, or a hybrid approach.
 */
public enum AnalysisMethod {
    
    /** Analysis performed using AI/LLM (Groq, OpenAI, etc.) */
    AI("AI", "Analysis performed by large language model"),
    
    /** Analysis performed using keyword/rule-based approach */
    KEYWORD("Keyword", "Rule-based keyword matching analysis"),
    
    /** Analysis performed using both AI and keyword methods */
    HYBRID("Hybrid", "Combined AI and keyword-based analysis");
    
    private final String displayName;
    private final String description;
    
    AnalysisMethod(String displayName, String description) {
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
     * Check if this method uses AI.
     */
    public boolean usesAI() {
        return this == AI || this == HYBRID;
    }
    
    /**
     * Check if this method uses keywords.
     */
    public boolean usesKeywords() {
        return this == KEYWORD || this == HYBRID;
    }
}
