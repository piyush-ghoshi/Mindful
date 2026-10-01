package com.mindful.wellness.ai.provider;

import com.mindful.wellness.ai.model.ConversationContext;
import com.mindful.wellness.dto.MoodAnalysisResult;

import java.util.List;

/**
 * Common abstraction for LLM and conversational AI providers.
 *
 * Enables multi-provider support (Groq, OpenAI, Ollama, etc.) with automatic
 * failover to resilient clinical fallback handlers.
 */
public interface AiProvider {

    /**
     * Generate an empathetic, conversational response for MindBot companion mode.
     *
     * @param context Rich conversation history, user identity, and emotional trajectory
     * @return Formatted response string, or null if generation failed
     */
    String generateCompanionReply(ConversationContext context);

    /**
     * Analyze user message for mood classification, emotional intensity, and clinical indicators.
     *
     * @param messageContent User's text
     * @param conversationContext Recent turns for grounding
     * @return MoodAnalysisResult or null if analysis failed
     */
    MoodAnalysisResult analyzeMood(String messageContent, List<String> conversationContext);

    /**
     * Generate structured JSON mental health assessment report.
     *
     * @param userName Name of student
     * @param conversationSummary Condensed dialogue points
     * @param detectedSeverity Calculated severity tag
     * @return JSON string complying with report schema
     */
    String generateReportJson(String userName, String conversationSummary, String detectedSeverity);

    /**
     * Check if this provider has valid credentials and connectivity.
     */
    boolean isAvailable();

    /**
     * Human-readable identifier for this provider (e.g., "Groq-Llama3.1", "RuleBasedFallback").
     */
    String getProviderName();
}
