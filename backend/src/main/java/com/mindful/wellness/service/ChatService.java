package com.mindful.wellness.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindful.wellness.dto.ChatMessageDto;
import com.mindful.wellness.dto.ChatSessionDto;
import com.mindful.wellness.dto.MentalHealthReportDto;
import com.mindful.wellness.entity.*;
import com.mindful.wellness.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core MindBot chat service.
 *
 * Handles:
 * - Session creation (ASSESSMENT or CASUAL)
 * - Rate limiting (BASE: 10 msgs/day, 1 report/day | PREMIUM: 50 msgs/day, 2 reports/day)
 * - Scripted assessment conversation flow
 * - Severity detection (keyword + score based)
 * - Mental health report generation
 * - Emergency / severe case handling
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final MentalHealthReportRepository reportRepo;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final GroqService groqService;

    // ── Rate limits ────────────────────────────────────────────────────────────
    private static final int BASE_MSG_LIMIT    = 10;
    private static final int BASE_REPORT_LIMIT = 1;
    private static final int PRO_MSG_LIMIT     = 50;
    private static final int PRO_REPORT_LIMIT  = 2;

    // ── Assessment question flow ───────────────────────────────────────────────
    private static final List<String> ASSESSMENT_QUESTIONS = List.of(
        "Hi there 💚 I'm MindBot, your private wellness companion. Everything you share here stays completely confidential — this is your safe space.\n\nLet's start gently. **How have you been feeling overall this past week?** You can describe it in your own words — there's no right or wrong answer.",
        "Thank you for sharing that. I hear you. 🌿\n\n**On a scale of 1 to 10, how would you rate your mood today?** (1 = very low, 10 = excellent)",
        "**How has your sleep been lately?** Are you sleeping too much, too little, or having trouble falling/staying asleep?",
        "**How is your energy and motivation?** Are everyday tasks feeling manageable, or does everything feel like a lot of effort?",
        "**Have you been able to connect with friends, family, or anyone you trust recently?** How has your social life been feeling?",
        "**Have you experienced any anxious thoughts, racing mind, or persistent worries lately?** If so, what kinds of things tend to trigger them?",
        "**How is your appetite and self-care routine?** (Eating, hygiene, exercise — anything you feel comfortable sharing.)",
        "Almost done 🌱 **Is there one thing that has been weighing on your mind the most recently?** You don't have to share details — even a word or phrase is fine.",
        "**Finally — have you had any thoughts of harming yourself or feeling like things would be better without you?** Please be honest. There is no judgment here, only support.",
        "Thank you so much for trusting me with all of this. I'm generating your personalised report now... 📋"
    );

    private static final List<String> CASUAL_TOPICS = List.of(
        "anxiety", "stress", "depression", "sleep", "mood", "worry", "fear",
        "sad", "lonely", "overwhelmed", "burnout", "panic", "grief", "trauma",
        "self-esteem", "confidence", "relationships", "anger", "focus", "motivation"
    );

    // Severity keywords
    private static final List<String> SEVERE_KEYWORDS = List.of(
        "suicide", "suicidal", "kill myself", "end my life", "don't want to live",
        "self-harm", "cutting", "hurting myself", "want to die", "no reason to live"
    );
    private static final List<String> HIGH_KEYWORDS = List.of(
        "hopeless", "worthless", "can't go on", "can't cope", "falling apart",
        "breaking down", "crisis", "desperate", "unbearable", "no way out"
    );
    private static final List<String> MODERATE_KEYWORDS = List.of(
        "anxious", "depressed", "lonely", "overwhelmed", "stressed", "panicking",
        "can't sleep", "no energy", "lost interest", "giving up"
    );

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Start a new chat session.
     * Enforces daily message rate limits before creating.
     */
    public ChatSessionDto startSession(UUID userId, String sessionType) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        // Check if user already has an active session — resume it if types match
        Optional<ChatSession> existing = sessionRepo.findByUserIdAndIsActiveTrue(userId);
        if (existing.isPresent()) {
            ChatSession ext = existing.get();
            if (ext.getSessionType().equalsIgnoreCase(sessionType)) {
                return toDto(ext, userId);
            } else {
                // End the old active session since the user is starting a different type
                endSession(ext);
            }
        }

        // Rate limit check
        long msgsToday = countMessagesToday(userId, startOfDay);
        int limit = getMessageLimit(userId);
        if (msgsToday >= limit) {
            throw new IllegalStateException("Daily chat limit reached (" + limit + " messages). Upgrade to Premium for more.");
        }

        ChatSession session = ChatSession.builder()
                .userId(userId)
                .sessionType(sessionType.toUpperCase())
                .messageCount(0)
                .reportGenerated(false)
                .isActive(true)
                .build();
        session = sessionRepo.save(session);

        // Send opening message from bot
        String opening = "ASSESSMENT".equals(sessionType.toUpperCase())
                ? ASSESSMENT_QUESTIONS.get(0)
                : buildCasualOpening();

        saveMessage(session.getId(), userId, "BOT", opening, null);

        log.info("Chat session started: {} type={} user={}", session.getId(), sessionType, userId);
        return toDto(session, userId);
    }

    /**
     * End the current active chat session for a user.
     */
    public void endActiveSession(UUID userId) {
        sessionRepo.findByUserIdAndIsActiveTrue(userId).ifPresent(this::endSession);
    }

    /**
     * Get the current active session for a user.
     */
    @Transactional(readOnly = true)
    public Optional<ChatSessionDto> getActiveSession(UUID userId) {
        return sessionRepo.findByUserIdAndIsActiveTrue(userId)
                .map(s -> toDto(s, userId));
    }

    /**
     * Send a user message and get the bot reply.
     */
    public ChatMessageDto sendMessage(UUID userId, UUID sessionId, String content, String uploadedReportText) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }
        if (!session.getIsActive()) {
            throw new IllegalStateException("This session has ended. Start a new session.");
        }

        // Rate limit: count user messages today
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long msgsToday = countMessagesToday(userId, startOfDay);
        int limit = getMessageLimit(userId);
        if (msgsToday >= limit) {
            throw new IllegalStateException("Daily message limit reached (" + limit + "). Come back tomorrow or upgrade to Premium.");
        }

        // Detect severity
        String severity = detectSeverity(content);
        if (uploadedReportText != null && !uploadedReportText.isBlank()) {
            content = "[UPLOADED REPORT]\n" + uploadedReportText.trim() + "\n\n[USER MESSAGE]\n" + content;
        }

        // Save user message
        saveMessage(session.getId(), userId, "USER", content, severity);
        session.setMessageCount(session.getMessageCount() + 1);

        // Update session severity if worse
        if (severity != null) {
            session.setDetectedSeverity(worseSeverity(session.getDetectedSeverity(), severity));
        }

        // Generate bot reply
        String botReply = generateBotReply(session, content, severity, uploadedReportText);
        saveMessage(session.getId(), userId, "BOT", botReply, null);

        sessionRepo.save(session);

        // If SEVERE — end session immediately after crisis message
        if ("SEVERE".equals(severity) || "SEVERE".equals(session.getDetectedSeverity())) {
            endSession(session);
            log.warn("SEVERE severity detected for user {}", userId);
        }

        // If this was the last assessment question, end and generate report
        int userMsgCount = (int) messageRepo.countBySessionIdAndRole(session.getId(), "USER");
        if ("ASSESSMENT".equals(session.getSessionType()) && userMsgCount >= ASSESSMENT_QUESTIONS.size() - 1) {
            endSession(session);
        }

        return ChatMessageDto.builder()
                .role("BOT").content(botReply)
                .sessionId(sessionId)
                .severityFlag(severity)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Generate the mental health report for a completed assessment session.
     */
    public MentalHealthReportDto generateReport(UUID userId, UUID sessionId) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        // Check daily report limit
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long reportsToday = sessionRepo.countTodaysReports(userId, startOfDay);
        int reportLimit = getReportLimit(userId);
        if (reportsToday >= reportLimit) {
            throw new IllegalStateException("Daily report limit reached (" + reportLimit + "). Upgrade to Premium for more.");
        }

        // Return existing report if already generated
        Optional<MentalHealthReport> existing = reportRepo.findBySessionId(sessionId);
        if (existing.isPresent()) {
            return toReportDto(existing.get(), userId);
        }

        // Gather user messages
        List<ChatMessage> messages = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<String> userMessages = messages.stream()
                .filter(m -> "USER".equals(m.getRole()))
                .map(ChatMessage::getContent)
                .collect(Collectors.toList());

        String severity = session.getDetectedSeverity();
        if (severity == null) severity = "LOW";

        User user = userRepository.findById(userId).orElse(null);
        String userName = user != null ? user.getFullName() : "User";

        int phaseNum = reportRepo.nextPhaseNumber(userId);
        String phaseTitle = "Phase " + phaseNum + " — " + phaseLabel(severity);
        String conversationSummary = userMessages.stream().limit(15).collect(Collectors.joining(" | "));

        List<String> conditions;
        List<String> exercises;
        List<String> meditations;
        String conclusion;
        boolean referral;
        int wellnessScore;

        try {
            String reportJsonStr = groqService.generateReportJson(userName, conversationSummary, severity);
            com.fasterxml.jackson.databind.JsonNode reportNode = objectMapper.readTree(reportJsonStr);

            conditions    = fromJsonNode(reportNode.get("conditionPoints"));
            exercises     = fromJsonNode(reportNode.get("recommendedExercises"));
            meditations   = fromJsonNode(reportNode.get("recommendedMeditations"));
            conclusion    = reportNode.has("conclusion") ? reportNode.get("conclusion").asText() : buildConclusion(severity, 50, conditions);
            referral      = reportNode.has("counsellorReferralSuggested") && reportNode.get("counsellorReferralSuggested").asBoolean();
            wellnessScore = reportNode.has("wellnessScore") ? reportNode.get("wellnessScore").asInt(50) : computeWellnessScore(severity, userMessages);
        } catch (Exception e) {
            log.warn("Groq report generation failed, using fallback: {}", e.getMessage());
            conditions    = analyseConditionPoints(userMessages, severity);
            exercises     = recommendedExercises(severity);
            meditations   = recommendedMeditations(severity);
            wellnessScore = computeWellnessScore(severity, userMessages);
            conclusion    = buildConclusion(severity, wellnessScore, conditions);
            referral      = "HIGH".equals(severity) || "SEVERE".equals(severity);
        }

        // Ensure exactly 5 items in each list
        while (conditions.size()  < 5) conditions.add("General wellness monitoring recommended");
        while (exercises.size()   < 5) exercises.add("Daily 10-minute mindful movement");
        while (meditations.size() < 5) meditations.add("5-minute breathing exercise morning and evening");

        MentalHealthReport report = MentalHealthReport.builder()
                .userId(userId)
                .sessionId(sessionId)
                .phaseNumber(phaseNum)
                .title(phaseTitle)
                .mentalStateLevel(severity)
                .wellnessScore(wellnessScore)
                .conditionPoints(toJson(conditions))
                .recommendedExercises(toJson(exercises))
                .recommendedMeditations(toJson(meditations))
                .conclusion(conclusion)
                .counsellorReferralSuggested(referral)
                .reportJson("{}")
                .build();

        report = reportRepo.save(report);
        session.setReportGenerated(true);
        sessionRepo.save(session);

        log.info("Report generated for user {}: phase={} level={}", userId, phaseNum, severity);

        MentalHealthReportDto dto = toReportDto(report, userId);
        if (user != null) {
            dto.setUserName(user.getFullName());
            dto.setUserEmail(user.getEmail());
        }
        return dto;
    }

    /**
     * Get all reports for a user.
     */
    @Transactional(readOnly = true)
    public List<MentalHealthReportDto> getUserReports(UUID userId) {
        return reportRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(r -> toReportDto(r, userId))
                .collect(Collectors.toList());
    }

    /**
     * Get a specific report.
     */
    @Transactional(readOnly = true)
    public MentalHealthReportDto getReport(UUID userId, UUID reportId) {
        MentalHealthReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        if (!report.getUserId().equals(userId)) throw new IllegalArgumentException("Access denied");
        MentalHealthReportDto dto = toReportDto(report, userId);
        userRepository.findById(userId).ifPresent(u -> {
            dto.setUserName(u.getFullName());
            dto.setUserEmail(u.getEmail());
        });
        return dto;
    }

    /**
     * Get all messages for a session.
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getSessionMessages(UUID userId, UUID sessionId) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!session.getUserId().equals(userId)) throw new IllegalArgumentException("Access denied");
        return messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream().map(this::toMessageDto).collect(Collectors.toList());
    }

    /**
     * Get rate limit info for the current user.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRateLimitInfo(UUID userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long msgsToday    = countMessagesToday(userId, startOfDay);
        long reportsToday = sessionRepo.countTodaysReports(userId, startOfDay);
        int msgLimit      = getMessageLimit(userId);
        int reportLimit   = getReportLimit(userId);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("messagesUsedToday",  msgsToday);
        info.put("messagesDailyLimit", msgLimit);
        info.put("reportsUsedToday",   reportsToday);
        info.put("reportsDailyLimit",  reportLimit);
        info.put("canChat",    msgsToday < msgLimit);
        info.put("canReport",  reportsToday < reportLimit);
        return info;
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void endSession(ChatSession session) {
        session.setIsActive(false);
        session.setEndedAt(LocalDateTime.now());
        sessionRepo.save(session);
    }

    private ChatMessage saveMessage(UUID sessionId, UUID userId, String role, String content, String severity) {
        return messageRepo.save(ChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .role(role)
                .content(content)
                .severityFlag(severity)
                .build());
    }

    private String generateBotReply(ChatSession session, String userContent, String severity, String uploaded) {
        // SEVERE → immediate crisis response (don't wait for LLM)
        if ("SEVERE".equals(severity) || "SEVERE".equals(session.getDetectedSeverity())) {
            return "🚨 I'm really concerned about what you've shared. **Please reach out for immediate support right now:**\n\n" +
                   "• **iCall (India):** 9152987821\n" +
                   "• **Vandrevala Foundation:** 1860-2662-345 (24/7)\n" +
                   "• **AASRA:** 9820466627 (24/7)\n\n" +
                   "You are not alone, and what you're feeling is real. I'm generating an urgent report and flagging this for counsellor review. " +
                   "Please talk to someone you trust right now. 💚\n\n" +
                   "**Your session has been saved and a report is ready for you.**";
        }

        // Build conversation history for Groq
        List<ChatMessage> history = messageRepo.findBySessionIdOrderByCreatedAtAsc(session.getId());
        List<Map<String, String>> conversationHistory = history.stream()
                .map(m -> Map.of(
                    "role", "USER".equals(m.getRole()) ? "user" : "assistant",
                    "content", m.getContent()
                ))
                .collect(Collectors.toList());

        // Prefix context for assessment vs casual
        String contextualContent = userContent;
        if (uploaded != null && !uploaded.isBlank()) {
            contextualContent = "[Previous Report Uploaded]\n" + uploaded + "\n\n[User Message]\n" + userContent;
        }

        // For assessment: add context about which question we're on
        if ("ASSESSMENT".equals(session.getSessionType())) {
            int questionNum = (int) messageRepo.countBySessionIdAndRole(session.getId(), "USER");
            int totalQuestions = ASSESSMENT_QUESTIONS.size() - 1;
            if (questionNum < totalQuestions) {
                String nextQuestionText = ASSESSMENT_QUESTIONS.get(questionNum);
                contextualContent = String.format("[Assessment Question %d of %d]\nUser reply: %s\n\nInstructions: Validate the user's feelings/reply empathetically, then ask the next question exactly or naturally:\n%s", 
                    questionNum, totalQuestions, userContent, nextQuestionText);
            } else {
                contextualContent = String.format("[Final assessment question answered]\nUser reply: %s\n\nInstructions: Validate the user's final answer, summarize the assessment, and tell them that their report is ready to be generated.", userContent);
            }
        }

        return groqService.chat(conversationHistory, contextualContent);
    }

    private String buildCasualOpening() {
        return "Hello! I'm MindBot 💚 Your private, confidential wellness companion.\n\n" +
               "*\"Your feelings are valid. This space is safe. Share as much or as little as you feel comfortable with.\"*\n\n" +
               "Everything you share here is **completely private** — not disclosed anywhere, not shared with anyone without your consent.\n\n" +
               "I can help with:\n" +
               "• Anxiety, stress & burnout\n" +
               "• Sleep, mood & energy\n" +
               "• Loneliness, relationships & grief\n" +
               "• Self-esteem & confidence\n\n" +
               "What's been on your mind lately?";
    }

    private String detectSeverity(String content) {
        if (content == null) return null;
        String lower = content.toLowerCase();
        if (SEVERE_KEYWORDS.stream().anyMatch(lower::contains)) return "SEVERE";
        if (HIGH_KEYWORDS.stream().anyMatch(lower::contains))   return "HIGH";
        if (MODERATE_KEYWORDS.stream().anyMatch(lower::contains)) return "MODERATE";
        return "LOW";
    }

    private String worseSeverity(String current, String incoming) {
        List<String> order = List.of("LOW", "MODERATE", "HIGH", "SEVERE");
        int ci = order.indexOf(current == null ? "LOW" : current);
        int ii = order.indexOf(incoming == null ? "LOW" : incoming);
        return ii > ci ? incoming : current;
    }

    private int computeWellnessScore(String severity, List<String> messages) {
        // Base score from severity
        int base = switch (severity) {
            case "SEVERE" -> 15;
            case "HIGH"   -> 35;
            case "MODERATE" -> 55;
            default       -> 70;
        };
        // Adjust for positive language
        long positiveCount = messages.stream()
                .filter(m -> m.toLowerCase().matches(".*(better|happy|okay|good|improved|hopeful|well|positive|grateful|calm).*"))
                .count();
        return Math.min(100, base + (int)(positiveCount * 5));
    }

    private String phaseLabel(String severity) {
        return switch (severity) {
            case "SEVERE" -> "Crisis Assessment";
            case "HIGH"   -> "Urgent Check-In";
            case "MODERATE" -> "Wellness Check";
            default        -> "Initial Assessment";
        };
    }

    private List<String> analyseConditionPoints(List<String> messages, String severity) {
        List<String> points = new ArrayList<>();
        String combined = String.join(" ", messages).toLowerCase();

        if (combined.contains("sleep") || combined.contains("tired"))
            points.add("Reports disrupted sleep patterns or persistent fatigue affecting daily functioning");
        if (combined.contains("anxious") || combined.contains("worry") || combined.contains("panic"))
            points.add("Experiences anxiety symptoms including worry, restlessness, or panic episodes");
        if (combined.contains("sad") || combined.contains("depress") || combined.contains("low mood"))
            points.add("Describes low mood, sadness, or reduced interest in previously enjoyed activities");
        if (combined.contains("stress") || combined.contains("overwhelm") || combined.contains("pressure"))
            points.add("Under significant stress with signs of emotional overwhelm");
        if (combined.contains("lonely") || combined.contains("isolat") || combined.contains("alone"))
            points.add("Experiencing social withdrawal or feelings of isolation");

        // Ensure exactly 5 points
        while (points.size() < 5) {
            points.add(switch (points.size()) {
                case 0 -> "General emotional distress reported across multiple life domains";
                case 1 -> "Fluctuating energy levels impacting academic or professional performance";
                case 2 -> "Challenges with emotional regulation and coping strategies";
                case 3 -> "Self-care routines showing signs of disruption";
                default -> "Benefit from structured mental wellness support and monitoring";
            });
        }
        return points.stream().limit(5).collect(Collectors.toList());
    }

    private List<String> recommendedExercises(String severity) {
        return switch (severity) {
            case "SEVERE", "HIGH" -> List.of(
                "Gentle 10-minute walk in natural light daily",
                "Box breathing (4-4-4-4) — 3 rounds, 3x daily",
                "Progressive Muscle Relaxation — 15 min before bed",
                "Gentle yoga or stretching (YouTube: Yoga with Adriene)",
                "Limit vigorous exercise until stability improves"
            );
            case "MODERATE" -> List.of(
                "30-minute brisk walk or jog, 5 days/week",
                "Morning sun exposure for 10 minutes (mood regulation)",
                "Body scan meditation — 10 minutes daily",
                "Dance or free movement to favourite music for 5 minutes",
                "Stretching routine upon waking and before sleep"
            );
            default -> List.of(
                "20-30 minute cardio 4-5x per week (running, cycling, swimming)",
                "Strength training 2-3x per week",
                "Nature walk without phone — 15 minutes",
                "Yoga or Pilates 2x per week",
                "Active social sports or group fitness"
            );
        };
    }

    private List<String> recommendedMeditations(String severity) {
        return switch (severity) {
            case "SEVERE", "HIGH" -> List.of(
                "Guided crisis calm: search 'Calm: Breathe' on YouTube",
                "5-4-3-2-1 Grounding technique — 3x daily",
                "Loving-Kindness meditation — 10 minutes morning",
                "Headspace 'SOS' sessions (free on Headspace app)",
                "Body scan with breath focus — 15 min before sleep"
            );
            case "MODERATE" -> List.of(
                "10-minute morning mindfulness with Insight Timer (free)",
                "RAIN technique: Recognize, Allow, Investigate, Nurture",
                "Gratitude journaling — 3 things each evening",
                "Mindful eating — one meal per day without distractions",
                "Box breathing before any stressful situation"
            );
            default -> List.of(
                "20-minute mindfulness meditation, morning",
                "Journaling: stream-of-consciousness for 10 minutes",
                "Visualization of goals and positive outcomes",
                "Digital detox hour before bed",
                "Weekly reflection: What went well? What can I improve?"
            );
        };
    }

    private String buildConclusion(String severity, int score, List<String> conditions) {
        return switch (severity) {
            case "SEVERE" ->
                "This assessment indicates a high-priority mental health concern requiring immediate professional support. " +
                "The individual is encouraged to connect with a licensed counsellor or crisis service as soon as possible. " +
                "MindBot has flagged this session for counsellor review. Please do not navigate this alone — help is available.";
            case "HIGH" ->
                "The assessment reflects significant emotional distress across several areas of wellbeing. " +
                "While coping, the individual would greatly benefit from regular sessions with a qualified counsellor. " +
                "The recommended exercises and meditations above can provide interim relief, but professional support is strongly advised.";
            case "MODERATE" ->
                "The assessment reveals moderate stress and emotional challenges that are impacting daily wellbeing. " +
                "With consistent self-care practices, peer support, and potentially a few counselling sessions, " +
                "meaningful improvement is very achievable. You are taking a positive step by engaging with MindBot today.";
            default ->
                "Overall wellbeing appears relatively stable, with some areas to nurture. " +
                "Continuing regular check-ins, maintaining healthy routines, and staying connected to your support network " +
                "will help sustain and improve your mental wellness. Keep up the great work!";
        };
    }

    private long countMessagesToday(UUID userId, LocalDateTime startOfDay) {
        return sessionRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(s -> s.getCreatedAt().isAfter(startOfDay))
                .mapToLong(s -> s.getMessageCount() == null ? 0 : s.getMessageCount())
                .sum();
    }

    private int getMessageLimit(UUID userId) {
        // TODO: check subscription tier — for now everyone is BASE
        return BASE_MSG_LIMIT;
    }

    private int getReportLimit(UUID userId) {
        return BASE_REPORT_LIMIT;
    }

    private String toJson(List<String> list) {
        try { return objectMapper.writeValueAsString(list); } catch (Exception e) { return "[]"; }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try { return objectMapper.readValue(json, new TypeReference<>() {}); } catch (Exception e) { return new ArrayList<>(); }
    }

    private List<String> fromJsonNode(com.fasterxml.jackson.databind.JsonNode node) {
        if (node == null || !node.isArray()) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        node.forEach(n -> result.add(n.asText()));
        return result;
    }

    // ── Converters ─────────────────────────────────────────────────────────────

    private ChatSessionDto toDto(ChatSession s, UUID userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<ChatMessageDto> msgs = messageRepo.findBySessionIdOrderByCreatedAtAsc(s.getId())
                .stream().map(this::toMessageDto).collect(Collectors.toList());
        long msgsToday    = countMessagesToday(userId, startOfDay);
        long reportsToday = sessionRepo.countTodaysReports(userId, startOfDay);
        return ChatSessionDto.builder()
                .id(s.getId())
                .sessionType(s.getSessionType())
                .messageCount(s.getMessageCount())
                .reportGenerated(s.getReportGenerated())
                .detectedSeverity(s.getDetectedSeverity())
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt())
                .messages(msgs)
                .messagesUsedToday((int) msgsToday)
                .messagesDailyLimit(getMessageLimit(userId))
                .reportsUsedToday((int) reportsToday)
                .reportsDailyLimit(getReportLimit(userId))
                .build();
    }

    private ChatMessageDto toMessageDto(ChatMessage m) {
        return ChatMessageDto.builder()
                .id(m.getId()).sessionId(m.getSessionId())
                .role(m.getRole()).content(m.getContent())
                .severityFlag(m.getSeverityFlag())
                .createdAt(m.getCreatedAt()).build();
    }

    private MentalHealthReportDto toReportDto(MentalHealthReport r, UUID userId) {
        return MentalHealthReportDto.builder()
                .id(r.getId()).userId(r.getUserId()).sessionId(r.getSessionId())
                .phaseNumber(r.getPhaseNumber()).title(r.getTitle())
                .mentalStateLevel(r.getMentalStateLevel())
                .wellnessScore(r.getWellnessScore())
                .conditionPoints(fromJson(r.getConditionPoints()))
                .recommendedExercises(fromJson(r.getRecommendedExercises()))
                .recommendedMeditations(fromJson(r.getRecommendedMeditations()))
                .conclusion(r.getConclusion())
                .counsellorReferralSuggested(r.getCounsellorReferralSuggested())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
