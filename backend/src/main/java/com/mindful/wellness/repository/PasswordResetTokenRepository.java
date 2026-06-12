package com.mindful.wellness.repository;

import com.mindful.wellness.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PasswordResetToken entity providing database access operations.
 * 
 * Validates: Requirement 1 (Student Authentication and Registration)
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Find a password reset token by token string.
     * 
     * @param token the token string
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find an unused password reset token for a user.
     * 
     * @param userId the user ID
     * @return Optional containing the unused token if found
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.userId = :userId AND prt.isUsed = false AND prt.expiryDate > CURRENT_TIMESTAMP")
    Optional<PasswordResetToken> findByUserIdAndIsUsedFalse(@Param("userId") UUID userId);

    /**
     * Delete all expired password reset tokens.
     * 
     * This method removes tokens that have passed their expiry date.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();

    /**
     * Delete all tokens for a specific user.
     * 
     * @param userId the user ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Find all unused tokens for a user.
     * 
     * @param userId the user ID
     * @return list of unused tokens for the user
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.userId = :userId AND prt.isUsed = false")
    java.util.List<PasswordResetToken> findAllUnusedByUserId(@Param("userId") UUID userId);

    /**
     * Check if a token exists and is valid (not expired and not used).
     * 
     * @param token the token string
     * @return true if a valid token exists
     */
    @Query("SELECT CASE WHEN COUNT(prt) > 0 THEN true ELSE false END FROM PasswordResetToken prt WHERE prt.token = :token AND prt.isUsed = false AND prt.expiryDate > CURRENT_TIMESTAMP")
    boolean existsValidToken(@Param("token") String token);
}
