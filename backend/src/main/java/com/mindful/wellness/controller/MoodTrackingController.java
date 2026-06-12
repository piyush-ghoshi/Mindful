package com.mindful.wellness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindful.wellness.dto.MoodEntryDto;
import com.mindful.wellness.entity.MoodEntry;
import com.mindful.wellness.service.MoodTrackingService;
import com.mindful.wellness.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for mood tracking write operations (recording entries).
 *
 * Read operations (history, stats) live in MoodHistoryController to avoid
 * duplicate route mappings on the same @RequestMapping("/api/mood") prefix.
 */
@RestController
@RequestMapping("/api/mood")
@RequiredArgsConstructor
@Slf4j
public class MoodTrackingController {

    private final MoodTrackingService moodTrackingService;
    private final AuthUtil authUtil;
    private final ObjectMapper objectMapper;

    /**
     * Record a new mood entry for the authenticated student.
     *
     * POST /api/mood/entries
     */
    @PostMapping("/entries")
    public ResponseEntity<MoodEntryDto> recordMoodEntry(
            Authentication authentication,
            @RequestBody MoodEntryDto moodEntryDto) {
        UUID studentId = authUtil.getUserId(authentication);

        String emotionsJson = serializeList(moodEntryDto.getEmotions());
        String triggersJson = serializeList(moodEntryDto.getTriggers());
        String activitiesJson = serializeList(moodEntryDto.getActivities());

        // Convert DTO → entity (only the fields the client should set)
        MoodEntry moodEntry = MoodEntry.builder()
                .moodRating(moodEntryDto.getMoodRating())
                .energyLevel(moodEntryDto.getEnergyLevel())
                .sleepQuality(moodEntryDto.getSleepQuality())
                .journalText(moodEntryDto.getJournalText())
                .emotions(emotionsJson)
                .triggers(triggersJson)
                .activities(activitiesJson)
                .isPrivate(moodEntryDto.getIsPrivate() != null ? moodEntryDto.getIsPrivate() : false)
                .build();

        MoodEntryDto recorded = moodTrackingService.recordMoodEntry(studentId, moodEntry);
        log.info("Mood entry recorded for student {}", studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(recorded);
    }

    private String serializeList(java.util.List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("Failed to serialize list to JSON: {}", list, e);
            return "[]";
        }
    }
}

