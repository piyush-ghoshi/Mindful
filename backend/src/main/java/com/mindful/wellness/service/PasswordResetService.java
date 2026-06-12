package com.mindful.wellness.service;

import com.mindful.wellness.entity.PasswordResetToken;
import com.mindful.wellness.entity.User;
import com.mindful.wellness.repository.PasswordResetTokenRepository;
import com.mindful.wellness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Handles password reset token generation, validation, and password update.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    /** Token valid for 1 hour */
    private static final long TOKEN_EXPIRY_HOURS = 1;

    /**
     * Generate a reset token and send it via email.
     * Silently succeeds even if the email is not registered (prevents enumeration).
     */
    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // Invalidate any existing tokens for this user
            tokenRepository.findAllUnusedByUserId(user.getId())
                    .forEach(t -> { t.setIsUsed(true); tokenRepository.save(t); });

            String rawToken = UUID.randomUUID().toString();
            PasswordResetToken token = PasswordResetToken.builder()
                    .token(rawToken)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                    .isUsed(false)
                    .build();
            tokenRepository.save(token);

            String resetLink = "http://localhost:5174/reset-password?token=" + rawToken;
            notificationService.createNotification(
                    user.getId(),
                    "PASSWORD_RESET",
                    "Reset your Mindful password",
                    "Click the link below to reset your password (valid for 1 hour):\n\n" + resetLink
                            + "\n\nIf you did not request this, ignore this email.",
                    "EMAIL"
            );
            log.info("Password reset initiated for user {}", user.getId());
        });
    }

    /**
     * Validate a reset token and update the user's password.
     *
     * @param rawToken   the token from the reset link
     * @param newPassword the new plain-text password
     * @throws IllegalArgumentException if the token is invalid/expired
     */
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (!token.isValid()) {
            throw new IllegalArgumentException("Reset token has expired or already been used");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.markAsUsed();
        tokenRepository.save(token);

        log.info("Password reset completed for user {}", user.getId());
    }
}
