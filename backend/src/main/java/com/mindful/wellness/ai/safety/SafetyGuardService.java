package com.mindful.wellness.ai.safety;

import com.mindful.wellness.ai.model.SafetyCheckResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Safety and clinical guardrails service.
 *
 * Implements:
 * 1. Pre-LLM crisis screening: Immediate interception of high-lethality suicidal and self-harm ideation.
 * 2. Post-LLM clinical validation: Screening for harmful medical diagnosis or prescription hallucinations.
 */
@Slf4j
@Service
public class SafetyGuardService {

    // Critical triggers requiring immediate crisis interception
    private static final List<String> CRISIS_TRIGGERS = List.of(
            "kill myself", "want to die", "commit suicide", "end my life",
            "end it all", "better off dead", "cutting myself", "hang myself",
            "take all my pills", "slit my wrists", "no reason to live",
            "suicidal", "planning to die", "jump off", "goodbye world"
    );

    // Crisis response text with 24/7 verified Indian mental health crisis resources
    public static final String CRISIS_RESPONSE = """
            I hear how much pain you are in right now, and I care deeply about your safety. 🌿 Please know you don't have to carry this alone.

            **Immediate Crisis Support (Free & Confidential — Available 24/7):**
            - **Tele-MANAS (Govt of India):** Dial `14416` or `1800-891-4416` (24/7)
            - **KIRAN Mental Health Helpline:** `1800-599-0019` (24/7)
            - **Vandrevala Foundation:** `9999 666 555` or `1860-2662-345` (24/7)
            - **iCall Helpline:** `9152987821` (Mon-Sat, 8 AM - 10 PM)
            - **AASRA:** `9820466627` (24/7)

            Please reach out to one of these helplines or contact someone you trust right now. I am also flagging this session so our university wellness team is aware and can support you. 💚
            """;

    /**
     * Pre-LLM safety evaluation.
     * Evaluates incoming message content for acute danger before any external LLM is called.
     */
    public SafetyCheckResult evaluateInput(String message) {
        if (message == null || message.isBlank()) {
            return SafetyCheckResult.safe();
        }

        String lower = message.toLowerCase();

        for (String trigger : CRISIS_TRIGGERS) {
            if (lower.contains(trigger)) {
                log.warn("SAFETY INTERCEPTION: Detected acute crisis trigger phrase='{}'", trigger);
                return SafetyCheckResult.crisis(
                        "SUICIDE_SELF_HARM",
                        CRISIS_RESPONSE,
                        trigger,
                        10.0
                );
            }
        }

        return SafetyCheckResult.safe();
    }

    /**
     * Post-LLM safety validation.
     * Ensures the LLM did not generate unauthorized medical diagnoses or drug prescriptions.
     */
    public String sanitizeOutput(String llmOutput) {
        if (llmOutput == null || llmOutput.isBlank()) {
            return llmOutput;
        }

        String lower = llmOutput.toLowerCase();

        // Check for medical prescription or clinical diagnosis violations
        boolean hasDiagnosis = lower.contains("i diagnose you with") ||
                lower.contains("you have been diagnosed with") ||
                lower.contains("my clinical diagnosis is");

        boolean hasPrescription = lower.contains("you should take prozac") ||
                lower.contains("take 50mg") ||
                lower.contains("take xanax") ||
                lower.contains("i prescribe");

        if (hasDiagnosis || hasPrescription) {
            log.warn("POST-LLM SANITIZATION TRIGGERED: Model attempted diagnosis or prescription. Replacing with safe fallback.");
            return "I am here as an emotional wellness companion to support and listen to you. " +
                    "For clinical diagnoses or medication questions, please consult directly with a licensed physician or psychiatrist. 🌿\n\n" +
                    "How are you feeling right now?";
        }

        return llmOutput;
    }
}
