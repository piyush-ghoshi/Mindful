package com.mindful.wellness.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mindful.wellness.ai.model.ConversationContext;
import com.mindful.wellness.ai.model.LlmMessage;
import com.mindful.wellness.dto.MoodAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Primary AI Provider utilizing Groq's high-speed Llama-3.1 model.
 */
@Slf4j
@Component("groqAiProvider")
public class GroqAiProvider implements AiProvider {

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.api.model:llama-3.1-8b-instant}")
    private String model;

    @Value("${groq.api.max-tokens:1024}")
    private int maxTokens;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are MindBot, an empathetic, supportive, and conversational mental wellness AI companion on the Mindful platform.
            You are speaking with university students to provide emotional support, active listening, mindfulness guidance, and healthy coping reflections.

            STUDENT PROFILE:
            - Name: %s
            - Detected Mood / State: %s
            - Recent Severity / Risk: %s

            CONVERSATION GUIDELINES:
            - Tone: Warm, grounded, compassionate, and non-judgmental. Talk like a caring, attentive friend.
            - Greet the user naturally and refer to them by their first name when appropriate.
            - Active Listening: Acknowledge and validate their emotional experience before offering suggestions.
            - Topics: You must welcome and empathetically discuss academic stress, burnout, grief, loneliness, sexual health, relationship conflicts, and emotional overwhelm. Treat these as valid wellness concerns.
            - Scope: If the student attempts to use you for non-wellness tasks (like coding assistance, math homework, or unrelated essays), politely reply:
              "I'm here to support your mental wellbeing and emotional health. Let's focus on how you're feeling and doing today."
            - Length: Keep responses conversational and concise (typically 80-150 words). Avoid overwhelming walls of text. Always end with an open, caring question or reflection to invite connection.

            CLINICAL BOUNDARIES:
            - NEVER diagnose clinical conditions (e.g. "You have major depressive disorder").
            - NEVER prescribe medications or alter medical treatment plans.
            - NEVER claim to replace a licensed psychiatrist, psychologist, or professional counsellor.
            - If in doubt, encourage connecting with trusted campus counselors or support networks.
            """;

    @Override
    public String generateCompanionReply(ConversationContext context) {
        if (!isAvailable()) {
            log.warn("Groq provider invoked but API key is not configured");
            return null;
        }

        try {
            ArrayNode messages = objectMapper.createArrayNode();

            // Build dynamic system prompt tailored to user context
            String studentName = (context.getUserName() != null && !context.getUserName().isBlank())
                    ? context.getUserName() : "Student";
            String moodState = (context.getCurrentMood() != null) ? context.getCurrentMood() : "Neutral";
            String severity = (context.getDetectedSeverity() != null) ? context.getDetectedSeverity() : "NORMAL";

            String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, studentName, moodState, severity);

            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);

            // Append conversational history (up to last 10 messages)
            List<LlmMessage> history = context.getHistory();
            int startIndex = Math.max(0, history.size() - 10);
            for (int i = startIndex; i < history.size(); i++) {
                LlmMessage msg = history.get(i);
                ObjectNode msgNode = objectMapper.createObjectNode();
                msgNode.put("role", msg.getRole());
                msgNode.put("content", msg.getContent());
                messages.add(msgNode);
            }

            // Append current user message
            ObjectNode currentMsg = objectMapper.createObjectNode();
            currentMsg.put("role", "user");
            currentMsg.put("content", context.getLatestUserMessage());
            messages.add(currentMsg);

            // Execute request
            return executeChatCompletion(messages, 0.7, 0.9);

        } catch (Exception e) {
            log.error("Groq generateCompanionReply failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public MoodAnalysisResult analyzeMood(String messageContent, List<String> conversationContext) {
        if (!isAvailable()) {
            return null;
        }

        String contextStr = (conversationContext != null && !conversationContext.isEmpty())
                ? String.join(" | ", conversationContext)
                : "No prior context";

        String prompt = String.format("""
                Analyze the following message for mood and mental health indicators.

                User message: "%s"
                Recent conversation context: %s

                Perform an emotional and psychological assessment. Return ONLY valid JSON (no markdown, no preamble):
                {
                  "mood": "SAD|HAPPY|ANXIOUS|ANGRY|OVERWHELMED|HOPELESS|CALM|NEUTRAL|FRUSTRATED|HOPEFUL",
                  "moodIntensity": 0.75,
                  "sentimentScore": -0.6,
                  "dominantEmotions": ["sadness", "fatigue", "worry"],
                  "riskIndicators": ["hopelessness", "isolation"],
                  "confidence": 0.85,
                  "rationale": "Brief explanation of the assessment"
                }

                Requirements:
                - mood: Choose the single dominant mood from the listed options.
                - moodIntensity: 0.0 (very mild) to 1.0 (overwhelming).
                - sentimentScore: -1.0 (extremely negative) to +1.0 (extremely positive).
                - dominantEmotions: 2 to 4 specific emotional descriptors.
                - riskIndicators: empty list if none, or specific flags like "suicidal_ideation", "hopelessness", "severe_isolation".
                """, messageContent, contextStr);

        try {
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);

            String response = executeChatCompletion(messages, 0.2, 0.9);
            if (response == null || response.isBlank()) {
                return null;
            }

            String json = extractJson(response);
            if (json == null) return null;

            return objectMapper.readValue(json, MoodAnalysisResult.class);

        } catch (Exception e) {
            log.error("Groq analyzeMood failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String generateReportJson(String userName, String conversationSummary, String detectedSeverity) {
        if (!isAvailable()) {
            return null;
        }

        String prompt = String.format("""
                Based on the following mental health assessment conversation, generate a structured clinical JSON report.

                User: %s
                Severity detected: %s
                Conversation summary: %s

                Return ONLY valid JSON in this exact format (no markdown, no explanation):
                {
                  "conditionPoints": ["point1", "point2", "point3", "point4", "point5"],
                  "recommendedExercises": ["exercise1", "exercise2", "exercise3", "exercise4", "exercise5"],
                  "recommendedMeditations": ["meditation1", "meditation2", "meditation3", "meditation4", "meditation5"],
                  "conclusion": "2-3 sentence professional summary",
                  "wellnessScore": 65,
                  "counsellorReferralSuggested": false
                }

                Rules:
                - conditionPoints: exactly 5 concise clinical observations
                - recommendedExercises: exactly 5 specific, actionable wellness exercises
                - recommendedMeditations: exactly 5 mindfulness or breathing practices
                - conclusion: compassionate, professional, 2-3 sentences max
                - wellnessScore: integer 0-100 (0=crisis, 100=excellent)
                - counsellorReferralSuggested: true if severity is HIGH or SEVERE
                """, userName, detectedSeverity, conversationSummary);

        try {
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);

            String response = executeChatCompletion(messages, 0.3, 0.9);
            if (response == null) return null;

            String json = extractJson(response);
            return (json != null) ? json : "{}";

        } catch (Exception e) {
            log.error("Groq generateReportJson failed: {}", e.getMessage());
            return null;
        }
    }

    private String executeChatCompletion(ArrayNode messages, double temperature, double topP) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.set("messages", messages);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            requestBody.put("top_p", topP);

            String bodyJson = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(bodyJson, JSON))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "no body";
                    log.error("Groq HTTP error {}: {}", response.code(), errBody);
                    return null;
                }

                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                return root.path("choices").get(0).path("message").path("content").asText();
            }

        } catch (Exception e) {
            log.error("Groq execution failed: {}", e.getMessage());
            return null;
        }
    }

    private String extractJson(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();

        if (trimmed.startsWith("```json")) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start != -1 && end != -1 && end > start) {
                return trimmed.substring(start, end + 1);
            }
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return trimmed.substring(start, end + 1);
        }

        return null;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.startsWith("gsk_placeholder");
    }

    @Override
    public String getProviderName() {
        return "Groq-" + model;
    }
}
