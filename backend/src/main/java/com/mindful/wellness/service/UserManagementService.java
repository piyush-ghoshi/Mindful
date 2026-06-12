package com.mindful.wellness.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindful.wellness.dto.*;
import com.mindful.wellness.entity.CounsellorProfile;
import com.mindful.wellness.entity.StudentProfile;
import com.mindful.wellness.entity.User;
import com.mindful.wellness.repository.CounsellorProfileRepository;
import com.mindful.wellness.repository.StudentProfileRepository;
import com.mindful.wellness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing user profiles and role-specific data.
 * 
 * Handles CRUD operations for user profiles, student profiles, and counsellor profiles.
 * Provides methods for retrieving and updating user information with role-specific attributes.
 * 
 * Validates: Requirements 1, 2, 3, 6, 12, 13, 15, 25, 26
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserManagementService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CounsellorProfileRepository counsellorProfileRepository;
    private final ObjectMapper objectMapper;

    /** Get all users with a given role */
    public List<com.mindful.wellness.entity.User> getUsersByRole(com.mindful.wellness.entity.UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Get a user by ID.
     * 
     * @param userId the user ID
     * @return the user
     * @throws IllegalArgumentException if user not found
     */
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    /** Get user by Firebase UID */
    public Optional<User> getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid);
    }

    /**
     * Get a user by email.
     * 
     * @param email the email address
     * @return Optional containing the user if found
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Update user profile information.
     * 
     * @param userId the user ID
     * @param request the profile update request
     * @return the updated user
     */
    public User updateUserProfile(UUID userId, ProfileUpdateRequest request) {
        User user = getUserById(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        if (request.getLanguagePreference() != null) {
            user.setLanguagePreference(request.getLanguagePreference());
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated for user ID: {}", userId);

        // Update student profile (auto-creates if it doesn't exist)
        updateStudentProfileFromRequest(userId, request);

        return updatedUser;
    }

    /**
     * Update language preference for a user.
     * 
     * @param userId the user ID
     * @param language the language preference
     * @return the updated user
     */
    public User updateLanguagePreference(UUID userId, String language) {
        User user = getUserById(userId);
        user.setLanguagePreference(language);
        User updatedUser = userRepository.save(user);
        log.info("Language preference updated for user ID: {} to {}", userId, language);
        return updatedUser;
    }

    /**
     * Get all counsellors matching the specified filters.
     * 
     * @param filters the counsellor filters
     * @return list of matching counsellors
     */
    public List<CounsellorProfileDto> getCounsellors(CounsellorFiltersDto filters) {
        List<CounsellorProfile> counsellors;

        if (filters.getInstitutionId() != null) {
            counsellors = counsellorProfileRepository.findByInstitutionId(filters.getInstitutionId());
        } else {
            counsellors = counsellorProfileRepository.findAll();
        }

        // Apply filters
        return counsellors.stream()
                .filter(c -> filters.getAcceptingNewStudents() == null || 
                           c.getIsAcceptingNewStudents().equals(filters.getAcceptingNewStudents()))
                .filter(c -> filters.getMinYearsOfExperience() == null || 
                           (c.getYearsOfExperience() != null && c.getYearsOfExperience() >= filters.getMinYearsOfExperience()))
                .filter(c -> filters.getMinRating() == null || 
                           c.getRating() >= filters.getMinRating())
                .map(this::convertCounsellorProfileToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a counsellor by ID.
     * 
     * @param counsellorId the counsellor ID
     * @return the counsellor profile DTO
     * @throws IllegalArgumentException if counsellor not found
     */
    public CounsellorProfileDto getCounsellorById(UUID counsellorId) {
        CounsellorProfile counsellor = counsellorProfileRepository.findByUserId(counsellorId)
                .orElseGet(() -> {
                    CounsellorProfile newProfile = CounsellorProfile.builder()
                            .userId(counsellorId)
                            .counsellorId("COUN-" + counsellorId.toString().substring(0, 8))
                            .licenseNumber("LIC-" + counsellorId.toString().substring(0, 8))
                            .rating(0.0)
                            .totalAppointments(0)
                            .maxAppointmentsPerDay(8)
                            .appointmentDuration(30)
                            .isAcceptingNewStudents(true)
                            .build();
                    return counsellorProfileRepository.save(newProfile);
                });
        return convertCounsellorProfileToDto(counsellor);
    }

    /**
     * Update counsellor professional profile details.
     * 
     * @param counsellorId the counsellor ID (User ID)
     * @param request the counsellor profile details
     * @return true if update was successful
     */
    public boolean updateCounsellorProfile(UUID counsellorId, CounsellorProfileDto request) {
        CounsellorProfile counsellor = counsellorProfileRepository.findByUserId(counsellorId)
                .orElseGet(() -> CounsellorProfile.builder()
                        .userId(counsellorId)
                        .counsellorId("COUN-" + counsellorId.toString().substring(0, 8))
                        .licenseNumber("LIC-" + counsellorId.toString().substring(0, 8))
                        .build());

        if (request.getLicenseNumber() != null) {
            counsellor.setLicenseNumber(request.getLicenseNumber());
        }
        if (request.getBio() != null) {
            counsellor.setBio(request.getBio());
        }
        if (request.getYearsOfExperience() != null) {
            counsellor.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getMaxAppointmentsPerDay() != null) {
            counsellor.setMaxAppointmentsPerDay(request.getMaxAppointmentsPerDay());
        }
        if (request.getAppointmentDuration() != null) {
            counsellor.setAppointmentDuration(request.getAppointmentDuration());
        }
        if (request.getIsAcceptingNewStudents() != null) {
            counsellor.setIsAcceptingNewStudents(request.getIsAcceptingNewStudents());
        }

        if (request.getSpecializations() != null) {
            try {
                counsellor.setSpecializations(objectMapper.writeValueAsString(request.getSpecializations()));
            } catch (Exception e) {
                log.warn("Error serializing specializations", e);
            }
        }

        if (request.getQualifications() != null) {
            try {
                counsellor.setQualifications(objectMapper.writeValueAsString(request.getQualifications()));
            } catch (Exception e) {
                log.warn("Error serializing qualifications", e);
            }
        }

        try {
            counsellorProfileRepository.save(counsellor);
            log.info("Counsellor profile updated for counsellor ID: {}", counsellorId);
            return true;
        } catch (Exception e) {
            log.error("Error updating counsellor profile", e);
            return false;
        }
    }

    /**
     * Get student profile by student ID.
     * 
     * @param studentId the student ID
     * @return the student profile DTO
     * @throws IllegalArgumentException if student profile not found
     */
    public StudentProfileDto getStudentProfile(UUID studentId) {
        StudentProfile studentProfile = studentProfileRepository.findByUserId(studentId)
                .orElseGet(() -> {
                    StudentProfile newProfile = StudentProfile.builder()
                            .userId(studentId)
                            .studentId("STU-" + studentId.toString().substring(0, 8))
                            .consentForDataSharing(false)
                            .consentForAnonymousAnalytics(false)
                            .build();
                    return studentProfileRepository.save(newProfile);
                });
        return convertStudentProfileToDto(studentProfile);
    }

    /**
     * Convert CounsellorProfile entity to DTO.
     * 
     * @param counsellor the counsellor profile entity
     * @return the counsellor profile DTO
     */
    private CounsellorProfileDto convertCounsellorProfileToDto(CounsellorProfile counsellor) {
        User user = getUserById(counsellor.getUserId());

        AvailabilityScheduleDto availabilitySchedule = null;
        if (counsellor.getAvailabilitySchedule() != null) {
            try {
                availabilitySchedule = objectMapper.readValue(
                        counsellor.getAvailabilitySchedule(),
                        AvailabilityScheduleDto.class
                );
            } catch (Exception e) {
                log.warn("Error deserializing availability schedule", e);
            }
        }

        List<String> specializations = null;
        if (counsellor.getSpecializations() != null) {
            try {
                specializations = objectMapper.readValue(
                        counsellor.getSpecializations(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
            } catch (Exception e) {
                log.warn("Error deserializing specializations", e);
            }
        }

        List<String> qualifications = null;
        if (counsellor.getQualifications() != null) {
            try {
                qualifications = objectMapper.readValue(
                        counsellor.getQualifications(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
            } catch (Exception e) {
                log.warn("Error deserializing qualifications", e);
            }
        }

        return CounsellorProfileDto.builder()
                .id(counsellor.getUserId())
                .userId(counsellor.getUserId())
                .counsellorId(counsellor.getCounsellorId())
                .institutionId(counsellor.getInstitutionId())
                .licenseNumber(counsellor.getLicenseNumber())
                .specializations(specializations)
                .qualifications(qualifications)
                .yearsOfExperience(counsellor.getYearsOfExperience())
                .bio(counsellor.getBio())
                .availabilitySchedule(availabilitySchedule)
                .maxAppointmentsPerDay(counsellor.getMaxAppointmentsPerDay())
                .appointmentDuration(counsellor.getAppointmentDuration())
                .rating(counsellor.getRating())
                .totalAppointments(counsellor.getTotalAppointments())
                .isAcceptingNewStudents(counsellor.getIsAcceptingNewStudents())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    /**
     * Convert StudentProfile entity to DTO.
     * 
     * @param studentProfile the student profile entity
     * @return the student profile DTO
     */
    private StudentProfileDto convertStudentProfileToDto(StudentProfile studentProfile) {
        NotificationPreferencesDto notificationPreferences = null;
        if (studentProfile.getNotificationPreferences() != null) {
            try {
                notificationPreferences = objectMapper.readValue(
                        studentProfile.getNotificationPreferences(),
                        NotificationPreferencesDto.class
                );
            } catch (Exception e) {
                log.warn("Error deserializing notification preferences", e);
            }
        }

        PrivacySettingsDto privacySettings = null;
        if (studentProfile.getPrivacySettings() != null) {
            try {
                privacySettings = objectMapper.readValue(
                        studentProfile.getPrivacySettings(),
                        PrivacySettingsDto.class
                );
            } catch (Exception e) {
                log.warn("Error deserializing privacy settings", e);
            }
        }

        List<String> wellnessGoals = null;
        if (studentProfile.getWellnessGoals() != null) {
            try {
                wellnessGoals = objectMapper.readValue(
                        studentProfile.getWellnessGoals(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
            } catch (Exception e) {
                log.warn("Error deserializing wellness goals", e);
            }
        }

        return StudentProfileDto.builder()
                .id(studentProfile.getId())
                .userId(studentProfile.getUserId())
                .studentId(studentProfile.getStudentId())
                .institutionId(studentProfile.getInstitutionId())
                .dateOfBirth(studentProfile.getDateOfBirth())
                .gender(studentProfile.getGender())
                .emergencyContactName(studentProfile.getEmergencyContactName())
                .emergencyContactPhone(studentProfile.getEmergencyContactPhone())
                .consentForDataSharing(studentProfile.getConsentForDataSharing())
                .consentForAnonymousAnalytics(studentProfile.getConsentForAnonymousAnalytics())
                .preferredCounsellorGender(studentProfile.getPreferredCounsellorGender())
                .wellnessGoals(wellnessGoals)
                .notificationPreferences(notificationPreferences)
                .privacySettings(privacySettings)
                .build();
    }

    /**
     * Update student profile from profile update request.
     * 
     * @param userId the user ID
     * @param request the profile update request
     */
    private void updateStudentProfileFromRequest(UUID userId, ProfileUpdateRequest request) {
        StudentProfile studentProfile = studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> StudentProfile.builder()
                        .userId(userId)
                        .studentId("STU-" + userId.toString().substring(0, 8))
                        .consentForDataSharing(false)
                        .consentForAnonymousAnalytics(false)
                        .build());

        if (request.getGender() != null) {
            studentProfile.setGender(request.getGender());
        }
        if (request.getEmergencyContactName() != null) {
            studentProfile.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            studentProfile.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
        if (request.getConsentForDataSharing() != null) {
            studentProfile.setConsentForDataSharing(request.getConsentForDataSharing());
        }
        if (request.getConsentForAnonymousAnalytics() != null) {
            studentProfile.setConsentForAnonymousAnalytics(request.getConsentForAnonymousAnalytics());
        }
        if (request.getPreferredCounsellorGender() != null) {
            studentProfile.setPreferredCounsellorGender(request.getPreferredCounsellorGender());
        }

        if (request.getWellnessGoals() != null) {
            try {
                studentProfile.setWellnessGoals(objectMapper.writeValueAsString(request.getWellnessGoals()));
            } catch (Exception e) {
                log.warn("Error serializing wellness goals", e);
            }
        }

        if (request.getNotificationPreferences() != null) {
            try {
                studentProfile.setNotificationPreferences(
                        objectMapper.writeValueAsString(request.getNotificationPreferences())
                );
            } catch (Exception e) {
                log.warn("Error serializing notification preferences", e);
            }
        }

        if (request.getPrivacySettings() != null) {
            try {
                studentProfile.setPrivacySettings(
                        objectMapper.writeValueAsString(request.getPrivacySettings())
                );
            } catch (Exception e) {
                log.warn("Error serializing privacy settings", e);
            }
        }

        studentProfileRepository.save(studentProfile);
        log.info("Student profile updated for user ID: {}", userId);
    }
}
