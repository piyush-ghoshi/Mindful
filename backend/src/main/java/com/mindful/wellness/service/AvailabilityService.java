package com.mindful.wellness.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindful.wellness.dto.AvailabilityScheduleDto;
import com.mindful.wellness.dto.DateExceptionDto;
import com.mindful.wellness.dto.TimeSlotDto;
import com.mindful.wellness.entity.AvailabilityException;
import com.mindful.wellness.entity.AvailabilitySchedule;
import com.mindful.wellness.repository.AvailabilityExceptionRepository;
import com.mindful.wellness.repository.AvailabilityScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing counsellor availability schedules and exceptions.
 * 
 * Handles setting availability schedules by day of week, managing time-off periods,
 * and checking availability for appointment booking.
 * 
 * Validates: Requirement 13
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AvailabilityService {

    private final AvailabilityScheduleRepository availabilityScheduleRepository;
    private final AvailabilityExceptionRepository availabilityExceptionRepository;
    private final ObjectMapper objectMapper;

    /**
     * Set availability schedule for a counsellor for a specific day of week.
     * 
     * @param counsellorId the counsellor ID
     * @param dayOfWeek the day of week
     * @param startTime the start time
     * @param endTime the end time
     * @param isAvailable whether the counsellor is available
     * @return true if the schedule was set successfully
     */
    public boolean setAvailability(UUID counsellorId, DayOfWeek dayOfWeek, LocalTime startTime, 
                                   LocalTime endTime, Boolean isAvailable) {
        try {
            // Validate times
            if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
                log.warn("Invalid time range: start time must be before end time");
                return false;
            }

            // Check if schedule already exists for this day
            Optional<AvailabilitySchedule> existingSchedule = 
                    availabilityScheduleRepository.findByCounsellorIdAndDayOfWeek(counsellorId, dayOfWeek);

            AvailabilitySchedule schedule;
            if (existingSchedule.isPresent()) {
                schedule = existingSchedule.get();
                schedule.setStartTime(startTime);
                schedule.setEndTime(endTime);
                schedule.setIsAvailable(isAvailable);
            } else {
                schedule = AvailabilitySchedule.builder()
                        .counsellorId(counsellorId)
                        .dayOfWeek(dayOfWeek)
                        .startTime(startTime)
                        .endTime(endTime)
                        .isAvailable(isAvailable)
                        .build();
            }

            availabilityScheduleRepository.save(schedule);
            log.info("Availability set for counsellor {} on {}: {} - {}", 
                    counsellorId, dayOfWeek, startTime, endTime);
            return true;
        } catch (Exception e) {
            log.error("Error setting availability for counsellor {}", counsellorId, e);
            return false;
        }
    }

    /**
     * Set complete availability schedule for a counsellor from DTO.
     * 
     * @param counsellorId the counsellor ID
     * @param scheduleDto the availability schedule DTO
     * @return true if the schedule was set successfully
     */
    public boolean setAvailabilitySchedule(UUID counsellorId, AvailabilityScheduleDto scheduleDto) {
        try {
            // Delete existing schedules for this counsellor
            availabilityScheduleRepository.deleteByCounsellorId(counsellorId);

            // Create new schedules from DTO
            Map<DayOfWeek, List<TimeSlotDto>> daySlots = new HashMap<>();
            daySlots.put(DayOfWeek.MONDAY, scheduleDto.getMonday());
            daySlots.put(DayOfWeek.TUESDAY, scheduleDto.getTuesday());
            daySlots.put(DayOfWeek.WEDNESDAY, scheduleDto.getWednesday());
            daySlots.put(DayOfWeek.THURSDAY, scheduleDto.getThursday());
            daySlots.put(DayOfWeek.FRIDAY, scheduleDto.getFriday());
            daySlots.put(DayOfWeek.SATURDAY, scheduleDto.getSaturday());
            daySlots.put(DayOfWeek.SUNDAY, scheduleDto.getSunday());

            for (Map.Entry<DayOfWeek, List<TimeSlotDto>> entry : daySlots.entrySet()) {
                DayOfWeek day = entry.getKey();
                List<TimeSlotDto> slots = entry.getValue();

                if (slots != null && !slots.isEmpty()) {
                    for (TimeSlotDto slot : slots) {
                        AvailabilitySchedule schedule = AvailabilitySchedule.builder()
                                .counsellorId(counsellorId)
                                .dayOfWeek(day)
                                .startTime(slot.getStartTime())
                                .endTime(slot.getEndTime())
                                .isAvailable(true)
                                .build();
                        availabilityScheduleRepository.save(schedule);
                    }
                }
            }

            log.info("Availability schedule set for counsellor {}", counsellorId);
            return true;
        } catch (Exception e) {
            log.error("Error setting availability schedule for counsellor {}", counsellorId, e);
            return false;
        }
    }

    /**
     * Add time-off for a counsellor on a specific date.
     * 
     * @param counsellorId the counsellor ID
     * @param date the date for time-off
     * @param reason the reason for time-off
     * @return true if time-off was added successfully
     */
    public boolean addTimeOff(UUID counsellorId, LocalDate date, String reason) {
        try {
            // Check if exception already exists
            if (availabilityExceptionRepository.existsByCounsellorIdAndExceptionDate(counsellorId, date)) {
                log.warn("Time-off already exists for counsellor {} on {}", counsellorId, date);
                return false;
            }

            AvailabilityException exception = AvailabilityException.builder()
                    .counsellorId(counsellorId)
                    .exceptionDate(date)
                    .isAvailable(false)
                    .reason(reason)
                    .build();

            availabilityExceptionRepository.save(exception);
            log.info("Time-off added for counsellor {} on {}: {}", counsellorId, date, reason);
            return true;
        } catch (Exception e) {
            log.error("Error adding time-off for counsellor {}", counsellorId, e);
            return false;
        }
    }

    /**
     * Remove time-off for a counsellor on a specific date.
     * 
     * @param counsellorId the counsellor ID
     * @param date the date to remove time-off from
     * @return true if time-off was removed successfully
     */
    public boolean removeTimeOff(UUID counsellorId, LocalDate date) {
        try {
            Optional<AvailabilityException> exception = 
                    availabilityExceptionRepository.findByCounsellorIdAndExceptionDate(counsellorId, date);

            if (exception.isPresent()) {
                availabilityExceptionRepository.delete(exception.get());
                log.info("Time-off removed for counsellor {} on {}", counsellorId, date);
                return true;
            } else {
                log.warn("No time-off found for counsellor {} on {}", counsellorId, date);
                return false;
            }
        } catch (Exception e) {
            log.error("Error removing time-off for counsellor {}", counsellorId, e);
            return false;
        }
    }

    /**
     * Get available time slots for a counsellor on a specific date.
     * 
     * @param counsellorId the counsellor ID
     * @param date the date to check availability
     * @return list of available time slots
     */
    public List<TimeSlotDto> getAvailableSlots(UUID counsellorId, LocalDate date) {
        try {
            List<TimeSlotDto> slots = new ArrayList<>();

            // Check if there's an exception for this date
            Optional<AvailabilityException> exception = 
                    availabilityExceptionRepository.findByCounsellorIdAndExceptionDate(counsellorId, date);

            if (exception.isPresent()) {
                AvailabilityException exc = exception.get();
                if (!exc.getIsAvailable()) {
                    // Counsellor is not available on this date
                    return slots;
                }
                // If available with custom slots, return those
                if (exc.getCustomSlots() != null) {
                    try {
                        List<TimeSlotDto> customSlots = objectMapper.readValue(
                                exc.getCustomSlots(),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, TimeSlotDto.class)
                        );
                        return customSlots;
                    } catch (Exception e) {
                        log.warn("Error deserializing custom slots", e);
                    }
                }
            }

            // Get regular schedule for the day of week
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            Optional<AvailabilitySchedule> schedule = 
                    availabilityScheduleRepository.findByCounsellorIdAndDayOfWeek(counsellorId, dayOfWeek);

            if (schedule.isPresent() && schedule.get().getIsAvailable()) {
                AvailabilitySchedule sched = schedule.get();
                TimeSlotDto slot = TimeSlotDto.builder()
                        .startTime(sched.getStartTime())
                        .endTime(sched.getEndTime())
                        .build();
                slots.add(slot);
            }

            return slots;
        } catch (Exception e) {
            log.error("Error getting available slots for counsellor {} on {}", counsellorId, date, e);
            return new ArrayList<>();
        }
    }

    /**
     * Check if a counsellor is available for a specific time slot.
     * 
     * @param counsellorId the counsellor ID
     * @param startTime the start time of the requested slot
     * @param endTime the end time of the requested slot
     * @return true if the counsellor is available for the entire time slot
     */
    public boolean isAvailable(UUID counsellorId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            LocalDate date = startTime.toLocalDate();
            LocalTime reqStartTime = startTime.toLocalTime();
            LocalTime reqEndTime = endTime.toLocalTime();

            // Check if there's an exception for this date
            Optional<AvailabilityException> exception = 
                    availabilityExceptionRepository.findByCounsellorIdAndExceptionDate(counsellorId, date);

            if (exception.isPresent()) {
                AvailabilityException exc = exception.get();
                if (!exc.getIsAvailable()) {
                    // Counsellor is not available on this date
                    return false;
                }
                // If available with custom slots, check those
                if (exc.getCustomSlots() != null) {
                    try {
                        List<TimeSlotDto> customSlots = objectMapper.readValue(
                                exc.getCustomSlots(),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, TimeSlotDto.class)
                        );
                        return isTimeSlotAvailable(customSlots, reqStartTime, reqEndTime);
                    } catch (Exception e) {
                        log.warn("Error deserializing custom slots", e);
                        return false;
                    }
                }
            }

            // Get regular schedule for the day of week
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            Optional<AvailabilitySchedule> schedule = 
                    availabilityScheduleRepository.findByCounsellorIdAndDayOfWeek(counsellorId, dayOfWeek);

            if (schedule.isPresent() && schedule.get().getIsAvailable()) {
                AvailabilitySchedule sched = schedule.get();
                return isTimeWithinSlot(reqStartTime, reqEndTime, sched.getStartTime(), sched.getEndTime());
            }

            return false;
        } catch (Exception e) {
            log.error("Error checking availability for counsellor {}", counsellorId, e);
            return false;
        }
    }

    /**
     * Get all availability schedules for a counsellor.
     * 
     * @param counsellorId the counsellor ID
     * @return list of availability schedules
     */
    public List<AvailabilitySchedule> getAvailabilitySchedules(UUID counsellorId) {
        return availabilityScheduleRepository.findByCounsellorId(counsellorId);
    }

    /**
     * Get all time-off exceptions for a counsellor.
     * 
     * @param counsellorId the counsellor ID
     * @return list of availability exceptions
     */
    public List<AvailabilityException> getTimeOffExceptions(UUID counsellorId) {
        return availabilityExceptionRepository.findByCounsellorId(counsellorId);
    }

    /**
     * Get time-off exceptions for a counsellor within a date range.
     * 
     * @param counsellorId the counsellor ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of availability exceptions within the date range
     */
    public List<AvailabilityException> getTimeOffExceptionsBetween(UUID counsellorId, LocalDate startDate, LocalDate endDate) {
        return availabilityExceptionRepository.findByCounsellorIdAndExceptionDateBetween(counsellorId, startDate, endDate);
    }

    /**
     * Check if a requested time slot is available within a list of available slots.
     * 
     * @param availableSlots the list of available time slots
     * @param reqStartTime the requested start time
     * @param reqEndTime the requested end time
     * @return true if the requested time is within one of the available slots
     */
    private boolean isTimeSlotAvailable(List<TimeSlotDto> availableSlots, LocalTime reqStartTime, LocalTime reqEndTime) {
        return availableSlots.stream()
                .anyMatch(slot -> isTimeWithinSlot(reqStartTime, reqEndTime, slot.getStartTime(), slot.getEndTime()));
    }

    /**
     * Check if a requested time is within a slot.
     * 
     * @param reqStartTime the requested start time
     * @param reqEndTime the requested end time
     * @param slotStartTime the slot start time
     * @param slotEndTime the slot end time
     * @return true if the requested time is within the slot
     */
    private boolean isTimeWithinSlot(LocalTime reqStartTime, LocalTime reqEndTime, 
                                     LocalTime slotStartTime, LocalTime slotEndTime) {
        return !reqStartTime.isBefore(slotStartTime) && !reqEndTime.isAfter(slotEndTime);
    }
}
