package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for time slot.
 * 
 * Validates: Requirements 1, 2, 3, 6, 13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDto {

    private LocalTime startTime;

    private LocalTime endTime;
}
