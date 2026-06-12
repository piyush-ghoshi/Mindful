package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for date exception in availability schedule.
 * 
 * Validates: Requirements 1, 2, 3, 6, 13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DateExceptionDto {

    private LocalDate date;

    private Boolean isAvailable;

    private List<TimeSlotDto> customSlots;
}
