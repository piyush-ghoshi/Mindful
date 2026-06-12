package com.mindful.wellness.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Public crisis support endpoint — no authentication required.
 * Returns helpline numbers and coping resources for the frontend crisis page.
 */
@RestController
@RequestMapping("/api/crisis")
public class CrisisSupportController {

    @GetMapping("/resources")
    public ResponseEntity<Map<String, Object>> getCrisisResources() {
        return ResponseEntity.ok(Map.of(
            "helplines", List.of(
                Map.of("name", "iCall (India)", "number", "9152987821", "hours", "Mon–Sat 8am–10pm"),
                Map.of("name", "Vandrevala Foundation", "number", "1860-2662-345", "hours", "24/7"),
                Map.of("name", "AASRA", "number", "9820466627", "hours", "24/7"),
                Map.of("name", "Snehi", "number", "044-24640050", "hours", "24/7"),
                Map.of("name", "National Suicide Prevention Lifeline (US)", "number", "988", "hours", "24/7")
            ),
            "copingTips", List.of(
                "Take slow, deep breaths — inhale for 4 counts, hold for 4, exhale for 4.",
                "Ground yourself: name 5 things you can see, 4 you can touch, 3 you can hear.",
                "Reach out to a trusted friend, family member, or counsellor.",
                "Remove yourself from the immediate stressful environment if safe to do so.",
                "Remember: this feeling is temporary and help is available."
            ),
            "emergencyNote", "If you are in immediate danger, please call your local emergency services (112 in India, 911 in the US)."
        ));
    }
}
