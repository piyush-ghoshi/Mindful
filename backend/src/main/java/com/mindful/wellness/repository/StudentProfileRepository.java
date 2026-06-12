package com.mindful.wellness.repository;

import com.mindful.wellness.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for StudentProfile entity providing database access operations.
 * 
 * Validates: Requirements 1, 2, 3, 25, 26
 */
@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {

    /**
     * Find a student profile by user ID.
     * 
     * @param userId the user ID
     * @return Optional containing the student profile if found
     */
    Optional<StudentProfile> findByUserId(UUID userId);

    /**
     * Find a student profile by student ID.
     * 
     * @param studentId the student ID
     * @return Optional containing the student profile if found
     */
    Optional<StudentProfile> findByStudentId(String studentId);

    /**
     * Check if a student profile exists for a user.
     * 
     * @param userId the user ID
     * @return true if student profile exists
     */
    boolean existsByUserId(UUID userId);
}
