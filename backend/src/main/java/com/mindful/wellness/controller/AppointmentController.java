package com.mindful.wellness.controller;

import com.mindful.wellness.dto.*;
import com.mindful.wellness.entity.Appointment;
import com.mindful.wellness.entity.AppointmentStatus;
import com.mindful.wellness.service.AppointmentService;
import com.mindful.wellness.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for appointment management endpoints.
 * Validates: Requirements 6, 13
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AuthUtil authUtil;

    /** GET /api/appointments — list appointments for current user (student or counsellor) */
    @GetMapping
    public ResponseEntity<List<AppointmentDto>> getAppointments(
            Authentication authentication,
            @RequestParam(required = false) String status) {
        try {
            UUID userId = authUtil.getUserId(authentication);
            AppointmentStatus appointmentStatus = null;
            if (status != null && !status.isEmpty()) {
                try { appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase()); } catch (Exception ignored) {}
            }

            List<Appointment> appointments;
            com.mindful.wellness.entity.UserRole role = authUtil.getCurrentUserRole();

            if (role == com.mindful.wellness.entity.UserRole.COUNSELLOR) {
                appointments = appointmentService.getCounsellorAppointmentsByUser(userId, appointmentStatus);
            } else {
                appointments = appointmentService.getStudentAppointments(userId, appointmentStatus);
            }

            return ResponseEntity.ok(appointments.stream()
                    .map(appointmentService::convertToDto)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Error retrieving appointments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** POST /api/appointments — book a new appointment */
    @PostMapping
    public ResponseEntity<AppointmentDto> bookAppointment(
            Authentication authentication,
            @Valid @RequestBody BookAppointmentRequest request) {
        try {
            UUID studentId = authUtil.getUserId(authentication);
            Appointment appointment = appointmentService.bookAppointment(
                    studentId,
                    request.getCounsellorId(),
                    request.getStartTime(),
                    request.getReason(),
                    request.getAppointmentType(),
                    request.getStudentNotes());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(appointmentService.convertToDto(appointment));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid appointment booking: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error booking appointment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** GET /api/appointments/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDto> getAppointmentById(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            UUID userId = authUtil.getUserId(authentication);
            boolean isAdmin = authUtil.getCurrentUserRole() == com.mindful.wellness.entity.UserRole.ADMIN;
            return ResponseEntity.ok(
                    appointmentService.convertToDto(appointmentService.getAppointmentById(id, userId, isAdmin)));
        } catch (IllegalArgumentException e) {
            log.warn("Access denied or appointment not found {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error retrieving appointment {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** PUT /api/appointments/{id} — reschedule */
    @PutMapping(value = {"/{id}", "/{id}/reschedule"})
    public ResponseEntity<AppointmentDto> rescheduleAppointment(
            @PathVariable UUID id,
            Authentication authentication,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        try {
            UUID userId = authUtil.getUserId(authentication);
            boolean isAdmin = authUtil.getCurrentUserRole() == com.mindful.wellness.entity.UserRole.ADMIN;
            return ResponseEntity.ok(appointmentService.convertToDto(
                    appointmentService.rescheduleAppointment(id, request.getNewStartTime(), userId, isAdmin)));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to reschedule appointment {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error rescheduling appointment {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** POST /api/appointments/{id}/cancel */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable UUID id,
            Authentication authentication,
            @RequestBody(required = false) CancelAppointmentRequest request) {
        try {
            UUID userId = authUtil.getUserId(authentication);
            boolean isAdmin = authUtil.getCurrentUserRole() == com.mindful.wellness.entity.UserRole.ADMIN;
            String reason = request != null ? request.getReason() : null;
            appointmentService.cancelAppointment(id, userId, reason, isAdmin);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to cancel appointment {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error cancelling appointment {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** POST /api/appointments/{id}/confirm — counsellor accepts a SCHEDULED appointment */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmAppointment(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            UUID userId = authUtil.getUserId(authentication);
            boolean isAdmin = authUtil.getCurrentUserRole() == com.mindful.wellness.entity.UserRole.ADMIN;
            Appointment confirmed = appointmentService.confirmAppointment(id, userId, isAdmin);
            return ResponseEntity.ok(appointmentService.convertToDto(confirmed));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error confirming appointment {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /** POST / PUT /api/appointments/{id}/complete */
    @RequestMapping(value = "/{id}/complete", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<Void> markAppointmentComplete(
            @PathVariable UUID id,
            Authentication authentication,
            @RequestParam(required = false) String notes,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            UUID userId = authUtil.getUserId(authentication);
            boolean isAdmin = authUtil.getCurrentUserRole() == com.mindful.wellness.entity.UserRole.ADMIN;
            String effectiveNotes = (notes != null && !notes.isBlank()) ? notes : (body != null ? body.get("notes") : null);
            appointmentService.markAppointmentComplete(id, effectiveNotes, userId, isAdmin);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to complete appointment {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error completing appointment {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** GET /api/appointments/upcoming */
    @GetMapping("/upcoming")
    public ResponseEntity<List<AppointmentDto>> getUpcomingAppointments(Authentication authentication) {
        try {
            UUID userId = authUtil.getUserId(authentication);
            return ResponseEntity.ok(appointmentService.getUpcomingAppointments(userId)
                    .stream().map(appointmentService::convertToDto).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Error retrieving upcoming appointments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** GET /api/appointments/availability/check */
    @GetMapping("/availability/check")
    public ResponseEntity<AvailabilityCheckResponse> checkAvailability(
            @RequestParam UUID counsellorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            boolean available = appointmentService.checkAvailability(counsellorId, startTime, endTime);
            return ResponseEntity.ok(AvailabilityCheckResponse.builder()
                    .available(available)
                    .counsellorId(counsellorId)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error checking availability", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
