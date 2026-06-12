package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for availability schedule.
 * 
 * Validates: Requirements 1, 2, 3, 6, 13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityScheduleDto {

    private List<TimeSlotDto> monday;

    private List<TimeSlotDto> tuesday;

    private List<TimeSlotDto> wednesday;

    private List<TimeSlotDto> thursday;

    private List<TimeSlotDto> friday;

    private List<TimeSlotDto> saturday;

    private List<TimeSlotDto> sunday;

    private List<DateExceptionDto> exceptions;
}
