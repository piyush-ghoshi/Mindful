package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for filtering counsellors.
 * 
 * Validates: Requirements 1, 2, 3, 6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounsellorFiltersDto {

    private UUID institutionId;

    private List<String> specializations;

    private Boolean acceptingNewStudents;

    private Integer minYearsOfExperience;

    private Double minRating;

    private String preferredGender;
}
