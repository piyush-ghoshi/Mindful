package com.mindful.wellness.controller;

import com.mindful.wellness.entity.User;
import com.mindful.wellness.entity.UserRole;
import com.mindful.wellness.repository.AppointmentRepository;
import com.mindful.wellness.repository.MoodEntryRepository;
import com.mindful.wellness.repository.NotificationRepository;
import com.mindful.wellness.repository.UserRepository;
import com.mindful.wellness.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin-only endpoints for platform management and analytics.
 *
 * All endpoints require ROLE_ADMIN.
 *
 * GET  /api/admin/dashboard          — platform-wide stats
 * GET  /api/admin/users              — list all users (paginated)
 * GET  /api/admin/users/{id}         — get user by ID
 * PUT  /api/admin/users/{id}/activate   — activate a user account
 * PUT  /api/admin/users/{id}/deactivate — deactivate a user account
 * GET  /api/admin/counsellors        — list all counsellors
 * GET  /api/admin/analytics          — usage analytics
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final MoodEntryRepository moodEntryRepository;
    private final NotificationRepository notificationRepository;
    private final UserManagementService userManagementService;

    /**
     * GET /api/admin/dashboard
     * Returns high-level platform statistics.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.findByRole(UserRole.STUDENT).size();
        long totalCounsellors = userRepository.findByRole(UserRole.COUNSELLOR).size();
        long totalAppointments = appointmentRepository.count();
        long totalMoodEntries = moodEntryRepository.count();

        // Active users in the last 30 days (users who logged in)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getLastLoginAt() != null && u.getLastLoginAt().isAfter(thirtyDaysAgo))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalStudents", totalStudents);
        stats.put("totalCounsellors", totalCounsellors);
        stats.put("totalAppointments", totalAppointments);
        stats.put("totalMoodEntries", totalMoodEntries);
        stats.put("activeUsersLast30Days", activeUsers);
        stats.put("generatedAt", LocalDateTime.now());

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/admin/users?page=0&size=20&role=STUDENT
     * List all users with optional role filter.
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<User> users;
        if (role != null && !role.isBlank()) {
            try {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                users = userRepository.findByRole(userRole);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid role: " + role));
            }
        } else {
            users = userRepository.findAll(PageRequest.of(page, Math.min(size, 100))).getContent();
        }

        List<Map<String, Object>> userList = users.stream()
                .map(this::toUserSummary)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "content", userList,
                "totalElements", userList.size()
        ));
    }

    /**
     * GET /api/admin/users/{id}
     * Get a specific user by ID.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(toUserSummary(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/admin/users/{id}/activate
     * Activate a user account.
     */
    @PutMapping("/users/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable UUID id) {
        return userRepository.findById(id).map(user -> {
            user.setIsActive(true);
            userRepository.save(user);
            log.info("Admin activated user {}", id);
            return ResponseEntity.ok(Map.of("message", "User activated", "userId", id));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/admin/users/{id}/deactivate
     * Deactivate a user account.
     */
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable UUID id) {
        return userRepository.findById(id).map(user -> {
            user.setIsActive(false);
            userRepository.save(user);
            log.info("Admin deactivated user {}", id);
            return ResponseEntity.ok(Map.of("message", "User deactivated", "userId", id));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/admin/counsellors
     * List all counsellors with their profile info.
     */
    @GetMapping("/counsellors")
    public ResponseEntity<?> getCounsellors() {
        List<Map<String, Object>> counsellors = userRepository.findByRole(UserRole.COUNSELLOR)
                .stream()
                .map(this::toUserSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("content", counsellors, "totalElements", counsellors.size()));
    }

    /**
     * GET /api/admin/analytics
     * Usage analytics for the past 30 days.
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime now = LocalDateTime.now();

        // New registrations in last 30 days
        long newUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();

        // Mood entries in last 30 days
        long recentMoodEntries = moodEntryRepository.findAll().stream()
                .filter(m -> m.getRecordedAt() != null && m.getRecordedAt().isAfter(thirtyDaysAgo))
                .count();

        // Appointments in last 30 days
        long recentAppointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("period", "Last 30 days");
        analytics.put("newRegistrations", newUsers);
        analytics.put("moodEntriesRecorded", recentMoodEntries);
        analytics.put("appointmentsBooked", recentAppointments);
        analytics.put("generatedAt", now);

        return ResponseEntity.ok(analytics);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Map<String, Object> toUserSummary(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("email", user.getEmail());
        map.put("firstName", user.getFirstName());
        map.put("lastName", user.getLastName());
        map.put("role", user.getRole());
        map.put("isActive", user.getIsActive());
        map.put("isEmailVerified", user.getIsEmailVerified());
        map.put("createdAt", user.getCreatedAt());
        map.put("lastLoginAt", user.getLastLoginAt());
        return map;
    }
}
