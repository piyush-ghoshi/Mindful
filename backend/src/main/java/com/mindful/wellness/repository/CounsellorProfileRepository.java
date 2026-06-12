package com.mindful.wellness.repository;

import com.mindful.wellness.entity.CounsellorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CounsellorProfile entity providing database access operations.
 * 
 * Validates: Requirements 1, 2, 3, 25, 26
 */
@Repository
public interface CounsellorProfileRepository extends JpaRepository<CounsellorProfile, UUID> {

    /**
     * Find a counsellor profile by user ID.
     * 
     * @param userId the user ID
     * @return Optional containing the counsellor profile if found
     */
    Optional<CounsellorProfile> findByUserId(UUID userId);

    /**
     * Find a counsellor profile by counsellor ID.
     * 
     * @param counsellorId the counsellor ID
     * @return Optional containing the counsellor profile if found
     */
    Optional<CounsellorProfile> findByCounsellorId(String counsellorId);

    /**
     * Find a counsellor profile by license number.
     * 
     * @param licenseNumber the license number
     * @return Optional containing the counsellor profile if found
     */
    Optional<CounsellorProfile> findByLicenseNumber(String licenseNumber);

    /**
     * Find all counsellors accepting new students.
     * 
     * @return list of counsellors accepting new students
     */
    @Query("SELECT cp FROM CounsellorProfile cp WHERE cp.isAcceptingNewStudents = true")
    List<CounsellorProfile> findAcceptingNewStudents();

    /**
     * Find all counsellors for an institution.
     * 
     * @param institutionId the institution ID
     * @return list of counsellors for the institution
     */
    List<CounsellorProfile> findByInstitutionId(UUID institutionId);

    /**
     * Check if a counsellor profile exists for a user.
     * 
     * @param userId the user ID
     * @return true if counsellor profile exists
     */
    boolean existsByUserId(UUID userId);

    /**
     * Check if a license number exists.
     * 
     * @param licenseNumber the license number
     * @return true if license number exists
     */
    boolean existsByLicenseNumber(String licenseNumber);
}
