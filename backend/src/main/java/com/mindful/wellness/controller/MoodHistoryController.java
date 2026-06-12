package com.mindful.wellness.controller;

import com.mindful.wellness.dto.MoodHistoryDto;
import com.mindful.wellness.dto.MoodStatsDto;
import com.mindful.wellness.service.MoodTrackingService;
import com.mindful.wellness.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for mood history and statistics.
 *
 * GET /api/mood/history  — paginated history for the authenticated student
 * GET /api/mood/stats    — statistics for the authenticated student
 * GET /api/mood/counsellor/student/{id}/history  — counsellor access
 * GET /api/mood/counsellor/student/{id}/stats    — counsellor access
 * GET /api/mood/counsellor/student/{id}/insights — counsellor access
 */
@RestController
@RequestMapping("/api/mood")
@RequiredArgsConstructor
@Slf4j
public class MoodHistoryController {

    private final MoodTrackingService moodTrackingService;
    private final AuthUtil authUtil;

    // ── Student endpoints ─────────────────────────────────────────────────────

    @GetMapping("/history")
    public ResponseEntity<MoodHistoryDto> getMoodHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID studentId = authUtil.getUserId(authentication);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(moodTrackingService.getMoodHistory(studentId, days, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<MoodStatsDto> getMoodStats(
            Authentication authentication,
            @RequestParam(defaultValue = "30") int days) {

        UUID studentId = authUtil.getUserId(authentication);
        return ResponseEntity.ok(moodTrackingService.getMoodStats(studentId, days));
    }

    // ── Counsellor endpoints ──────────────────────────────────────────────────

    @GetMapping("/counsellor/student/{studentId}/history")
    @PreAuthorize("hasRole('COUNSELLOR')")
    public ResponseEntity<MoodHistoryDto> getCounsellorStudentHistory(
            Authentication authentication,
            @PathVariable UUID studentId,
            @RequestParam(defaultValue = "90") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID counsellorId = authUtil.getUserId(authentication);
        if (!moodTrackingService.verifyCounsellorStudentAccess(counsellorId, studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(moodTrackingService.getMoodHistory(studentId, days, pageable));
    }

    @GetMapping("/counsellor/student/{studentId}/stats")
    @PreAuthorize("hasRole('COUNSELLOR')")
    public ResponseEntity<MoodStatsDto> getCounsellorStudentStats(
            Authentication authentication,
            @PathVariable UUID studentId,
            @RequestParam(defaultValue = "90") int days) {

        UUID counsellorId = authUtil.getUserId(authentication);
        if (!moodTrackingService.verifyCounsellorStudentAccess(counsellorId, studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(moodTrackingService.getMoodStats(studentId, days));
    }

    @GetMapping("/counsellor/student/{studentId}/insights")
    @PreAuthorize("hasRole('COUNSELLOR')")
    public ResponseEntity<MoodStatsDto> getCounsellorStudentInsights(
            Authentication authentication,
            @PathVariable UUID studentId) {

        UUID counsellorId = authUtil.getUserId(authentication);
        if (!moodTrackingService.verifyCounsellorStudentAccess(counsellorId, studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(moodTrackingService.getMoodStats(studentId, 90));
    }
}
