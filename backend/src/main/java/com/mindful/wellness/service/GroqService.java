package com.mindful.wellness.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mindful.wellness.dto.MoodAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
            You are MindBot, a casual, friendly, and empathetic AI companion on the Mindful platform. You are here to talk to university students about absolutely anything on their mind.

            PERSONALITY:
            - Friendly, casual, interactive, and easygoing. Talk like a supportive friend.
            - Warm, non-judgmental, and highly conversational.
            - Feel free to use a variety of emojis naturally to keep the conversation lively, engaging, and interactive.
            - Keep your tone relaxed, positive, and informal.

            CONVERSATION RULES:
            - Greet the user casually (e.g. if they say "hi" say "hello").
            - If they introduce their name (e.g. "I am Piyush"), greet them warmly by name (e.g. "Hello Piyush!").
            - You MUST accept and converse empathetically about sexual problems, intimacy issues, and relationship problems. Do not redirect these; treat them as valid wellness concerns.
            - If the user tries to misuse the chatbot for general educational queries, programming tasks, homework, math, or unrelated educational topics, you MUST reply: "I'm there to help your mental state and analyse your mental state, do not misuse me."
            - Keep your responses relatively short, conversational, and highly interactive. Always ask open-ended questions to keep the chat going.

            BOUNDARIES:
            - NEVER diagnose medical conditions or prescribe medications.
            - NEVER replace a licensed therapist or counsellor.

            CRISIS PROTOCOL — MANDATORY:
            If the user expresses suicidal ideation, self-harm, or immediate danger, ALWAYS:
            1. Acknowledge their pain with deep compassion.
            2. Provide these crisis resources:
               - iCall (India): 9152987821
               - Vandrevala Foundation: 1860-2662-345 (24/7)
               - AASRA: 9820466627 (24/7)
            3. Urge them to speak to someone trusted immediately.
            4. End with: "I'm flagging this session for counsellor review."

            FORMAT:
            - Keep casual chat responses under 150 words to keep it conversational.
            - Use **bold** for emphasis naturally.
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

                    // Return null so calling service can handle role/session-specific fallback
                    return null;
                }

                String responseBody = response.body().string();
                JsonNode json = objectMapper.readTree(responseBody);
                String content = json.path("choices").get(0).path("message").path("content").asText();

                log.debug("Groq response received ({} chars)", content.length());
                return content;
            }

        } catch (Exception e) {
            log.error("Groq API call failed: {}", e.getMessage());
            return null;
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
        if (response != null && response.contains("{")) {
            int start = response.indexOf('{');
            int end = response.lastIndexOf('}') + 1;
            if (end > start) return response.substring(start, end);
        }
        return "{}";
    }

    /**
     * Analyze mood and mental state using LLM.
     * 
     * @param messageContent The user message to analyze
     * @param conversationContext Recent conversation history (last 3-5 messages)
     * @return MoodAnalysisResult with detected mood, sentiment, and risk indicators
     */
    public MoodAnalysisResult analyzeMoodWithAI(String messageContent, List<String> conversationContext) {
        String contextStr = conversationContext != null && !conversationContext.isEmpty()
            ? String.join(" | ", conversationContext)
            : "No prior context";
        
        String prompt = String.format("""
            Analyze the following message for mood and mental health indicators.
            
            User message: "%s"
            
            Recent conversation context: %s
            
            Perform a detailed emotional and psychological analysis. Return ONLY valid JSON (no markdown, no explanation):
            {
              "mood": "SAD|HAPPY|ANXIOUS|ANGRY|OVERWHELMED|HOPELESS|CALM|NEUTRAL|FRUSTRATED|HOPEFUL",
              "moodIntensity": 0.75,
              "sentimentScore": -0.6,
              "dominantEmotions": ["sadness", "fatigue", "worry"],
              "riskIndicators": ["hopelessness", "isolation"],
              "confidence": 0.85,
              "rationale": "Brief explanation of the assessment"
            }
            
            Field descriptions:
            - mood: The PRIMARY mood (choose ONE from the list above)
            - moodIntensity: How strong is this mood? 0.0 (barely present) to 1.0 (overwhelming)
            - sentimentScore: Overall positivity/negativity: -1.0 (very negative) to 1.0 (very positive)
            - dominantEmotions: 2-4 specific emotions detected (lowercase, specific)
            - riskIndicators: Mental health concerns detected (empty array if none):
              * "suicidal_ideation" - thoughts of suicide
              * "self_harm_intent" - thoughts of hurting oneself
              * "hopelessness" - feeling no hope for future
              * "severe_isolation" - extreme loneliness/withdrawal
              * "panic_symptoms" - panic attack indicators
              * "substance_abuse_mention" - mentions of drugs/alcohol
            - confidence: Your confidence in this analysis (0.0 to 1.0)
            - rationale: 1-2 sentence explanation
            
            IMPORTANT:
            - Be sensitive and empathetic in your analysis
            - Err on the side of caution for risk indicators
            - Consider context from previous messages
            - If unsure, use lower confidence score
            """,
            messageContent,
            contextStr
        );
        
        try {
            String response = chat(Collections.emptyList(), prompt);
            
            if (response == null || response.trim().isEmpty()) {
                log.warn("Empty response from LLM for mood analysis");
                return null;
            }
            
            // Extract JSON from response (handle cases where model wraps in markdown)
            String jsonStr = extractJson(response);
            if (jsonStr == null) {
                log.warn("Could not extract JSON from LLM response");
                return null;
            }
            
            // Parse JSON to MoodAnalysisResult
            MoodAnalysisResult result = objectMapper.readValue(jsonStr, MoodAnalysisResult.class);
            
            // Validate result
            if (result.getMood() == null || result.getMoodIntensity() == null || result.getSentimentScore() == null) {
                log.warn("Incomplete mood analysis result from LLM");
                return null;
            }
            
            log.debug("LLM mood analysis successful: mood={}, intensity={}, sentiment={}, confidence={}", 
                result.getMood(), result.getMoodIntensity(), result.getSentimentScore(), result.getConfidence());
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to analyze mood with AI: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extract JSON from LLM response (handles markdown wrapping).
     */
    private String extractJson(String response) {
        if (response == null) {
            return null;
        }
        
        // Remove markdown code blocks if present
        String cleaned = response.trim();
        
        // Check for ```json ... ``` wrapper
        if (cleaned.startsWith("```json") || cleaned.startsWith("```")) {
            int start = cleaned.indexOf('{');
            int end = cleaned.lastIndexOf('}');
            if (start != -1 && end != -1 && end > start) {
                return cleaned.substring(start, end + 1);
            }
        }
        
        // Find JSON object boundaries
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        
        if (start != -1 && end != -1 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        
        return null;
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
