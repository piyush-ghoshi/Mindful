package com.mindful.wellness.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Groq LLM integration for MindBot AI responses.
 * Uses the llama3-8b-8192 model via Groq's OpenAI-compatible API.
 */
@Service
@Slf4j
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.api.model:llama3-8b-8192}")
    private String model;

    @Value("${groq.api.max-tokens:1024}")
    private int maxTokens;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * MindBot system prompt — sets the AI's personality and boundaries.
     */
    private static final String SYSTEM_PROMPT = """
            You are MindBot, an empathetic and professional AI mental wellness companion for the Mindful platform. You support university students with their mental health.

            PERSONALITY:
            - Warm, compassionate, non-judgmental
            - Use a gentle, conversational tone — never clinical or cold
            - Use emojis sparingly (💚 🌱 🌿) to feel warm, not playful
            - Always validate feelings before offering advice

            BOUNDARIES — STRICT:
            - ONLY discuss mental health topics: anxiety, stress, depression, sleep, mood, relationships, grief, self-esteem, burnout, trauma, academic pressure, loneliness
            - If asked about unrelated topics, gently redirect: "I'm specialised in mental wellness support. What's been on your mind lately?"
            - NEVER diagnose medical conditions
            - NEVER prescribe medications
            - NEVER replace a licensed therapist or counsellor

            CRISIS PROTOCOL — MANDATORY:
            If the user expresses suicidal ideation, self-harm, or immediate danger, ALWAYS:
            1. Acknowledge their pain with deep compassion
            2. Provide these crisis resources:
               - iCall (India): 9152987821
               - Vandrevala Foundation: 1860-2662-345 (24/7)
               - AASRA: 9820466627 (24/7)
            3. Urge them to speak to someone trusted immediately
            4. End with: "I'm flagging this session for counsellor review."

            FORMAT:
            - Keep responses under 200 words for casual chat
            - For assessment sessions, ask ONE clear question at a time
            - Use **bold** for key terms or action items
            - Use bullet lists for recommendations
            - Never use technical jargon

            PRIVACY REMINDER: Remind users occasionally that their conversations are completely private and confidential.
            """;

    /**
     * Send a conversation to Groq and get a response.
     *
     * @param conversationHistory List of {"role": "user"/"assistant", "content": "..."} maps
     * @param userMessage         The latest user message
     * @return The AI response text
     */
    public String chat(List<Map<String, String>> conversationHistory, String userMessage) {
        try {
            // Build messages array
            ArrayNode messages = objectMapper.createArrayNode();

            // System message
            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.add(systemMsg);

            // Conversation history (limit to last 10 turns to stay within context)
            int historyLimit = Math.max(0, conversationHistory.size() - 10);
            for (int i = historyLimit; i < conversationHistory.size(); i++) {
                Map<String, String> msg = conversationHistory.get(i);
                ObjectNode msgNode = objectMapper.createObjectNode();
                msgNode.put("role", msg.getOrDefault("role", "user"));
                msgNode.put("content", msg.getOrDefault("content", ""));
                messages.add(msgNode);
            }

            // Current user message
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            // Build request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.set("messages", messages);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", 0.7);
            requestBody.put("top_p", 0.9);

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
                    log.error("Groq API error {}: {}", response.code(), errBody);

                    // Graceful fallback
                    return getFallbackResponse(userMessage);
                }

                String responseBody = response.body().string();
                JsonNode json = objectMapper.readTree(responseBody);
                String content = json.path("choices").get(0).path("message").path("content").asText();

                log.debug("Groq response received ({} chars)", content.length());
                return content;
            }

        } catch (Exception e) {
            log.error("Groq API call failed: {}", e.getMessage());
            return getFallbackResponse(userMessage);
        }
    }

    /**
     * Generate the structured mental health report using Groq.
     */
    public String generateReportJson(String userName, String conversationSummary, String detectedSeverity) {
        String prompt = String.format("""
                Based on the following mental health assessment conversation, generate a structured JSON report.

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
                - recommendedExercises: exactly 5 specific, actionable exercises
                - recommendedMeditations: exactly 5 specific mindfulness/meditation practices
                - conclusion: compassionate, professional, 2-3 sentences max
                - wellnessScore: integer 0-100 (0=crisis, 100=excellent)
                - counsellorReferralSuggested: true if severity is HIGH or SEVERE
                """, userName, detectedSeverity, conversationSummary);

        List<Map<String, String>> empty = List.of();
        String response = chat(empty, prompt);

        // Extract JSON from response (handle cases where model wraps in markdown)
        if (response.contains("{")) {
            int start = response.indexOf('{');
            int end = response.lastIndexOf('}') + 1;
            if (end > start) return response.substring(start, end);
        }
        return "{}";
    }

    /**
     * Fallback response when Groq is unavailable.
     */
    private String getFallbackResponse(String userMessage) {
        String lower = userMessage.toLowerCase();
        if (lower.contains("anxious") || lower.contains("anxiety") || lower.contains("panic")) {
            return "I hear you — anxiety can feel really overwhelming. 💚\n\nTry this right now: **box breathing** — inhale for 4 seconds, hold for 4, exhale for 4, hold for 4. Repeat 3 times. It activates your body's calming response.\n\nWhat's been triggering the anxiety for you?";
        }
        if (lower.contains("sad") || lower.contains("depress") || lower.contains("down")) {
            return "Thank you for sharing that with me. 🌿 Feeling low is genuinely hard to carry.\n\nSmall steps matter — even a 10-minute walk in sunlight can shift your mood. Would you like to talk about what's been weighing on you?";
        }
        if (lower.contains("stress") || lower.contains("overwhelm")) {
            return "When everything piles up, it's easy to feel paralysed. 💚\n\nTry this: identify just **one thing** — the smallest possible action — that would make today better. What's the biggest stressor for you right now?";
        }
        return "Thank you for sharing with me. 💚 I'm here and I'm listening.\n\nCould you tell me a bit more about how you've been feeling? I want to make sure I understand what you're going through.";
    }
}
