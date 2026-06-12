package com.mindful.wellness.repository;

import com.mindful.wellness.entity.User;
import com.mindful.wellness.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity providing database access operations.
 * 
 * Validates: Requirements 1, 2, 3, 25, 26
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by email.
     * 
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by username.
     * 
     * @param username the username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by Firebase UID.
     * 
     * @param firebaseUid the Firebase UID
     * @return Optional containing the user if found
     */
    Optional<User> findByFirebaseUid(String firebaseUid);

    /**
     * Find an active user by email.
     * 
     * @param email the email address
     * @return Optional containing the active user if found
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findByEmailAndIsActiveTrue(@Param("email") String email);

    /**
     * Check if a user exists by email.
     * 
     * @param email the email address
     * @return true if user exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all users by role.
     * 
     * @param role the user role
     * @return list of users with the specified role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find all active users by role.
     * 
     * @param role the user role
     * @return list of active users with the specified role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    List<User> findAllByRoleAndIsActiveTrue(@Param("role") UserRole role);

    /**
     * Find all active users.
     * 
     * @return list of all active users
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllByIsActiveTrue();

    /**
     * Find all inactive users.
     * 
     * @return list of all inactive users
     */
    @Query("SELECT u FROM User u WHERE u.isActive = false")
    List<User> findAllByIsActiveFalse();

    /**
     * Find all users by role (active only).
     * 
     * @param role the user role
     * @return list of active users with the specified role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true ORDER BY u.createdAt DESC")
    List<User> findAllByRole(@Param("role") UserRole role);
}
