package com.mindful.wellness.controller;

import com.mindful.wellness.dto.AvailabilityScheduleDto;
import com.mindful.wellness.dto.TimeSlotDto;
import com.mindful.wellness.entity.AvailabilityException;
import com.mindful.wellness.entity.AvailabilitySchedule;
import com.mindful.wellness.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for counsellor availability management endpoints.
 * 
 * Provides endpoints for counsellors to set working hours, manage time-off,
 * and retrieve available appointment slots.
 * 
 * Validates: Requirement 13
 */
@RestController
@RequestMapping("/api/counsellors")
@RequiredArgsConstructor
@Slf4j
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    /**
     * Set availability schedule for a counsellor.
     * 
     * @param id the counsellor ID
     * @param availability the availability schedule DTO
     * @return success response
     */
    @PutMapping("/{id}/availability")
    public ResponseEntity<Void> setAvailability(
            @PathVariable UUID id,
            @RequestBody AvailabilityScheduleDto availability) {
        try {
            boolean success = availabilityService.setAvailabilitySchedule(id, availability);
            if (success) {
                log.info("Availability schedule set for counsellor {}", id);
                return ResponseEntity.noContent().build();
            } else {
                log.warn("Failed to set availability schedule for counsellor {}", id);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            log.error("Error setting availability for counsellor {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add time-off for a counsellor.
     * 
     * @param id the counsellor ID
     * @param date the date for time-off
     * @param reason the reason for time-off
     * @return success response
     */
    @PostMapping("/{id}/time-off")
    public ResponseEntity<Void> addTimeOff(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String reason) {
        try {
            boolean success = availabilityService.addTimeOff(id, date, reason);
            if (success) {
                log.info("Time-off added for counsellor {} on {}", id, date);
                return ResponseEntity.status(HttpStatus.CREATED).build();
            } else {
                log.warn("Failed to add time-off for counsellor {} on {}", id, date);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (Exception e) {
            log.error("Error adding time-off for counsellor {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Remove time-off for a counsellor.
     * 
     * @param id the counsellor ID
     * @param date the date to remove time-off from
     * @return success response
     */
    @DeleteMapping("/{id}/time-off/{date}")
    public ResponseEntity<Void> removeTimeOff(
            @PathVariable UUID id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            boolean success = availabilityService.removeTimeOff(id, date);
            if (success) {
                log.info("Time-off removed for counsellor {} on {}", id, date);
                return ResponseEntity.noContent().build();
            } else {
                log.warn("No time-off found for counsellor {} on {}", id, date);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error removing time-off for counsellor {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get available time slots for a counsellor on a specific date.
     * 
     * @param id the counsellor ID
     * @param date the date to check availability
     * @return list of available time slots
     */
    @GetMapping("/{id}/available-slots")
    public ResponseEntity<List<TimeSlotDto>> getAvailableSlots(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<TimeSlotDto> slots = availabilityService.getAvailableSlots(id, date);
            log.info("Retrieved available slots for counsellor {} on {}", id, date);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            log.error("Error retrieving available slots for counsellor {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all availability schedules for a counsellor.
     * 
     * @param id the counsellor ID
     * @return list of availability schedules
     */
    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<AvailabilitySchedule>> getAvailabilitySchedules(@PathVariable UUID id) {
        try {
            List<AvailabilitySchedule> schedules = availabilityService.getAvailabilitySchedules(id);
            log.info("Retrieved availability schedules for counsellor {}", id);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            log.error("Error retrieving availability schedules for counsellor {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all time-off exceptions for a counsellor.
     * 
     * @param id the counsellor ID
     * @return list of time-off exceptions
     */
    @GetMapping("/{id}/time-off")
    public ResponseEntity<List<AvailabilityException>> getTimeOffExceptions(@PathVariable UUID id) {
        try {
            List<AvailabilityException> exceptions = availabilityService.getTimeOffExceptions(id);
            log.info("Retrieved time-off exceptions for counsellor {}", id);
            return ResponseEntity.ok(exceptions);
        } catch (Exception e) {
            log.error("Error retrieving time-off exceptions for counsellor {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get time-off exceptions for a counsellor within a date range.
     * 
     * @param id the counsellor ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of time-off exceptions within the date range
     */
    @GetMapping("/{id}/time-off/range")
    public ResponseEntity<List<AvailabilityException>> getTimeOffExceptionsBetween(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AvailabilityException> exceptions = 
                    availabilityService.getTimeOffExceptionsBetween(id, startDate, endDate);
            log.info("Retrieved time-off exceptions for counsellor {} between {} and {}", id, startDate, endDate);
            return ResponseEntity.ok(exceptions);
        } catch (Exception e) {
            log.error("Error retrieving time-off exceptions for counsellor {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
