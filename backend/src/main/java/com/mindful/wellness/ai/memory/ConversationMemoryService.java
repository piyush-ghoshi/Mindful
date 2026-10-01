package com.mindful.wellness.ai.memory;

import com.mindful.wellness.ai.model.ConversationContext;
import com.mindful.wellness.ai.model.LlmMessage;
import com.mindful.wellness.entity.ChatMessage;
import com.mindful.wellness.entity.ChatSession;
import com.mindful.wellness.entity.User;
import com.mindful.wellness.repository.ChatMessageRepository;
import com.mindful.wellness.repository.ChatSessionRepository;
import com.mindful.wellness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service managing conversation memory, windowed dialogue history,
 * and contextual grounding for AI sessions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationMemoryService {

    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;
    private final UserRepository userRepository;

    private static final int MAX_MEMORY_TURNS = 10;

    /**
     * Assemble full ConversationContext for an ongoing session and new user input.
     *
     * @param userId The student's UUID
     * @param sessionId The active chat session UUID
     * @param latestUserMessage The newly received user message
     * @return Assembled ConversationContext ready for LLM consumption
     */
    @Transactional(readOnly = true)
    public ConversationContext buildContext(UUID userId, UUID sessionId, String latestUserMessage) {
        // 1. Resolve user profile
        User user = userRepository.findById(userId).orElse(null);
        String userName = (user != null && user.getFirstName() != null) ? user.getFirstName() : "Student";
        String language = (user != null && user.getLanguagePreference() != null) ? user.getLanguagePreference() : "en";

        // 2. Resolve session details
        ChatSession session = sessionRepository.findById(sessionId).orElse(null);
        String sessionType = (session != null && session.getSessionType() != null) ? session.getSessionType() : "CASUAL";
        String detectedSeverity = (session != null && session.getDetectedSeverity() != null) ? session.getDetectedSeverity() : "NORMAL";
        Integer highestRisk = (session != null) ? session.getHighestRiskScore() : 0;

        // 3. Resolve dialogue history
        List<ChatMessage> dbMessages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<LlmMessage> history = new ArrayList<>();

        // Keep last MAX_MEMORY_TURNS messages
        int startIndex = Math.max(0, dbMessages.size() - MAX_MEMORY_TURNS);
        for (int i = startIndex; i < dbMessages.size(); i++) {
            ChatMessage dbMsg = dbMessages.get(i);
            String role = "BOT".equalsIgnoreCase(dbMsg.getRole()) ? "assistant" : "user";
            history.add(LlmMessage.builder()
                    .role(role)
                    .content(dbMsg.getContent())
                    .timestamp(dbMsg.getCreatedAt())
                    .build());
        }

        return ConversationContext.builder()
                .userId(userId)
                .userName(userName)
                .sessionId(sessionId)
                .sessionType(sessionType)
                .languagePreference(language)
                .history(history)
                .latestUserMessage(latestUserMessage)
                .detectedSeverity(detectedSeverity)
                .highestRiskScore(highestRisk)
                .build();
    }

    /**
     * Extract recent dialogue as raw text list (useful for sentiment context).
     */
    @Transactional(readOnly = true)
    public List<String> getRecentContextSnippets(UUID sessionId, int count) {
        List<ChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<String> snippets = new ArrayList<>();
        int start = Math.max(0, messages.size() - count);
        for (int i = start; i < messages.size(); i++) {
            ChatMessage m = messages.get(i);
            snippets.add(m.getRole() + ": " + m.getContent());
        }
        return snippets;
    }
}
