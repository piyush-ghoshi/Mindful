package com.mindful.wellness.ai.provider;

import com.mindful.wellness.ai.model.ConversationContext;
import com.mindful.wellness.dto.MoodAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resilient, empathetic fallback provider.
 *
 * Ensures that if external AI services (like Groq) experience an outage, network timeout,
 * or rate limit, the student is ALWAYS met with compassionate, clinically sound mental health support.
 */
@Slf4j
@Component("fallbackCompanionProvider")
public class FallbackCompanionProvider implements AiProvider {

    @Override
    public String generateCompanionReply(ConversationContext context) {
        String message = context.getLatestUserMessage() != null ? context.getLatestUserMessage().toLowerCase() : "";
        String studentName = (context.getUserName() != null && !context.getUserName().isBlank())
                ? context.getUserName() : "";
        String greetingPrefix = !studentName.isBlank() ? "Hi " + studentName + ", " : "";

        // 1. Anxiety / Panic / Overwhelm
        if (containsAny(message, "anxious", "anxiety", "panic", "can't breathe", "racing heart", "scared", "terrified")) {
            return greetingPrefix + "I hear you, and it's completely okay to feel this way right now. 🌿 Anxiety can feel intense and physical.\n\n" +
                    "Let's try a quick grounding technique together: **Box Breathing**.\n" +
                    "1. Breathe in through your nose for **4 seconds**\n" +
                    "2. Hold your breath gently for **4 seconds**\n" +
                    "3. Exhale slowly through your mouth for **4 seconds**\n" +
                    "4. Rest for **4 seconds**\n\n" +
                    "Take one deep breath right now. When you're ready, what's been weighing on your mind the most today?";
        }

        // 2. Sadness / Depression / Emptiness
        if (containsAny(message, "sad", "depressed", "crying", "empty", "lonely", "numb", "exhausted", "tired of everything")) {
            return greetingPrefix + "thank you for being honest with me. Carrying heavy emotions can feel exhausting, and you don't have to carry it all alone. 💚\n\n" +
                    "Remember that healing isn't about giant leaps; tiny steps count just as much. Even stepping outside for 5 minutes of fresh air or drinking a glass of water is a victory today.\n\n" +
                    "Would you like to tell me more about what's been making you feel this way?";
        }

        // 3. Academic Stress / Exams / Burnout
        if (containsAny(message, "exam", "assignment", "study", "grades", "failure", "failed", "career", "pressure", "behind")) {
            return greetingPrefix + "university workload and academic pressure can become overwhelming so quickly. 📚\n\n" +
                    "Your worth is not defined by grades or productivity. When things pile up, try focusing on just **one single 15-minute task** today, then take a real break.\n\n" +
                    "Which assignment or subject is giving you the most stress right now? Let's break it down together.";
        }

        // 4. Relationships / Intimacy / Family Conflicts
        if (containsAny(message, "boyfriend", "girlfriend", "partner", "parents", "breakup", "relationship", "fight", "intimacy")) {
            return greetingPrefix + "relationships and personal connections have such a deep impact on our emotional wellbeing. It takes courage to reflect on this.\n\n" +
                    "It's completely normal to feel confused or hurt when relationships hit rough patches.\n\n" +
                    "What happened recently that's been lingering in your thoughts?";
        }

        // 5. Sleep Issues / Restlessness
        if (containsAny(message, "sleep", "insomnia", "awake", "nightmare", "can't sleep")) {
            return greetingPrefix + "sleep deprivation makes every single stressor feel twice as heavy. 🌙\n\n" +
                    "Try dimming the lights, keeping your phone away from bed, and practicing a 5-minute progressive muscle relaxation.\n\n" +
                    "Have you been having trouble falling asleep, or is your mind running through worries at night?";
        }

        // 6. Misuse / General tasks
        if (containsAny(message, "write code", "solve math", "write an essay", "python code", "java code")) {
            return "I'm here specifically to support your mental wellbeing, emotional health, and personal growth. Let's focus on how you're feeling and how your day is going.";
        }

        // Default supportive companion response
        return greetingPrefix + "thank you for sharing that with me. I'm right here listening, and whatever you're experiencing is valid. 🌿\n\n" +
                "Could you tell me a little more about what's going on? I'm here to support you.";
    }

    @Override
    public MoodAnalysisResult analyzeMood(String messageContent, List<String> conversationContext) {
        String lower = messageContent != null ? messageContent.toLowerCase() : "";
        String mood = "NEUTRAL";
        double sentiment = 0.0;
        double intensity = 0.5;
        List<String> emotions = new ArrayList<>();
        List<String> risks = new ArrayList<>();

        if (containsAny(lower, "happy", "great", "good", "excited", "grateful", "joy", "proud")) {
            mood = "HAPPY";
            sentiment = 0.7;
            emotions.add("optimism");
            emotions.add("gratitude");
        } else if (containsAny(lower, "calm", "peace", "relaxed", "fine", "okay")) {
            mood = "CALM";
            sentiment = 0.4;
            emotions.add("calmness");
        } else if (containsAny(lower, "hopeless", "no hope", "no point", "giving up", "pointless", "futile", "despairing", "desperate")) {
            mood = "HOPELESS";
            sentiment = -0.7;
            intensity = 0.85;
            emotions.add("despair");
            emotions.add("hopelessness");
            risks.add("hopelessness");
        } else if (containsAny(lower, "overwhelmed", "stressed", "too much", "can't cope", "drowning", "buried", "swamped")) {
            mood = "OVERWHELMED";
            sentiment = -0.55;
            intensity = 0.8;
            emotions.add("overwhelmed");
            emotions.add("stress");
        } else if (containsAny(lower, "anxious", "worry", "nervous", "scared", "fear", "panic", "tense", "uneasy")) {
            mood = "ANXIOUS";
            sentiment = -0.5;
            intensity = 0.75;
            emotions.add("anxiety");
            emotions.add("worry");
        } else if (containsAny(lower, "sad", "crying", "depress", "hurt", "grief", "down", "low", "unhappy", "miserable")) {
            mood = "SAD";
            sentiment = -0.6;
            intensity = 0.75;
            emotions.add("sadness");
        } else if (containsAny(lower, "angry", "furious", "mad", "livid", "outraged")) {
            mood = "ANGRY";
            sentiment = -0.6;
            intensity = 0.75;
            emotions.add("anger");
        } else if (containsAny(lower, "frustrated", "irritated", "annoyed", "stuck", "fed up")) {
            mood = "FRUSTRATED";
            sentiment = -0.4;
            intensity = 0.7;
            emotions.add("frustration");
        }

        return MoodAnalysisResult.builder()
                .mood(mood)
                .moodIntensity(java.math.BigDecimal.valueOf(intensity))
                .sentimentScore(java.math.BigDecimal.valueOf(sentiment))
                .dominantEmotions(emotions)
                .riskIndicators(risks)
                .confidence(java.math.BigDecimal.valueOf(0.70))
                .rationale("Rule-based psychological assessment fallback")
                .build();
    }

    @Override
    public String generateReportJson(String userName, String conversationSummary, String detectedSeverity) {
        String name = (userName != null && !userName.isBlank()) ? userName : "Student";
        String severity = (detectedSeverity != null) ? detectedSeverity : "MODERATE";

        int wellnessScore = switch (severity) {
            case "MINIMAL" -> 85;
            case "LOW" -> 72;
            case "HIGH" -> 40;
            case "SEVERE" -> 25;
            default -> 58;
        };

        boolean referral = "HIGH".equals(severity) || "SEVERE".equals(severity);

        return String.format("""
                {
                  "conditionPoints": [
                    "Active participation in self-reflection and dialogue",
                    "Emotional awareness of current situational stressors",
                    "Willingness to explore healthy coping strategies",
                    "Identification of recent emotional shifts and tension",
                    "Baseline cognitive resilience demonstrated"
                  ],
                  "recommendedExercises": [
                    "Box Breathing: 4-4-4-4 rhythm for acute tension relief",
                    "Daily 15-minute nature walk without screen distractions",
                    "5-4-3-2-1 Sensory Grounding exercise during distress",
                    "Structured journaling: 3 things you handled well today",
                    "Progressive muscle relaxation prior to bedtime"
                  ],
                  "recommendedMeditations": [
                    "Loving-kindness meditation for self-compassion",
                    "Body scan meditation for physical tension release",
                    "Mindful breathing for mental clarity",
                    "Guided grounding meditation for anxiety relief",
                    "Evening gratitude wind-down"
                  ],
                  "conclusion": "%s completed a thoughtful mental wellness session. Continuing regular check-ins and structured self-care routines will help build sustainable wellbeing.",
                  "wellnessScore": %d,
                  "counsellorReferralSuggested": %b
                }
                """, name, wellnessScore, referral);
    }

    private boolean containsAny(String text, String... words) {
        if (text == null) return false;
        for (String w : words) {
            if (text.contains(w)) return true;
        }
        return false;
    }

    @Override
    public boolean isAvailable() {
        return true; // Always operational
    }

    @Override
    public String getProviderName() {
        return "ClinicalRuleBasedFallback";
    }
}
