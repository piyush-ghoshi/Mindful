package com.mindful.wellness.ai.provider;

import com.mindful.wellness.ai.model.ConversationContext;
import com.mindful.wellness.dto.MoodAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provider Manager coordinating AI execution, provider selection, and graceful failover.
 *
 * Implements resilient multi-tier execution:
 * 1. Primary: Groq AI Provider (high-speed Llama-3.1 inference)
 * 2. Secondary/Fallback: Clinically sound rule-based companion provider
 */
@Slf4j
@Service
public class AiProviderManager {

    private final AiProvider primaryProvider;
    private final AiProvider fallbackProvider;

    public AiProviderManager(
            @Qualifier("groqAiProvider") AiProvider primaryProvider,
            @Qualifier("fallbackCompanionProvider") AiProvider fallbackProvider) {
        this.primaryProvider = primaryProvider;
        this.fallbackProvider = fallbackProvider;
    }

    /**
     * Generate companion response with automatic failover.
     */
    public String generateCompanionReply(ConversationContext context) {
        if (primaryProvider.isAvailable()) {
            try {
                String reply = primaryProvider.generateCompanionReply(context);
                if (reply != null && !reply.isBlank()) {
                    log.debug("Companion reply generated via {}", primaryProvider.getProviderName());
                    return reply;
                }
                log.warn("{} returned empty response. Failing over to {}",
                        primaryProvider.getProviderName(), fallbackProvider.getProviderName());
            } catch (Exception e) {
                log.error("{} execution error: {}. Failing over to {}",
                        primaryProvider.getProviderName(), e.getMessage(), fallbackProvider.getProviderName());
            }
        } else {
            log.info("{} is not configured/available. Using {}",
                    primaryProvider.getProviderName(), fallbackProvider.getProviderName());
        }

        return fallbackProvider.generateCompanionReply(context);
    }

    /**
     * Analyze mood with automatic failover.
     */
    public MoodAnalysisResult analyzeMood(String messageContent, List<String> conversationContext) {
        if (primaryProvider.isAvailable()) {
            try {
                MoodAnalysisResult result = primaryProvider.analyzeMood(messageContent, conversationContext);
                if (result != null && result.getMood() != null) {
                    return result;
                }
            } catch (Exception e) {
                log.warn("{} mood analysis failed: {}. Using fallback", primaryProvider.getProviderName(), e.getMessage());
            }
        }

        return fallbackProvider.analyzeMood(messageContent, conversationContext);
    }

    /**
     * Generate clinical report JSON with automatic failover.
     */
    public String generateReportJson(String userName, String conversationSummary, String detectedSeverity) {
        if (primaryProvider.isAvailable()) {
            try {
                String json = primaryProvider.generateReportJson(userName, conversationSummary, detectedSeverity);
                if (json != null && !json.isBlank() && json.contains("{")) {
                    return json;
                }
            } catch (Exception e) {
                log.warn("{} report generation failed: {}. Using fallback", primaryProvider.getProviderName(), e.getMessage());
            }
        }

        return fallbackProvider.generateReportJson(userName, conversationSummary, detectedSeverity);
    }

    public String getActivePrimaryProviderName() {
        return primaryProvider.isAvailable() ? primaryProvider.getProviderName() : "None (Fallback Active)";
    }
}
