package com.mindful.wellness.security.service;

import com.mindful.wellness.entity.User;
import com.mindful.wellness.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation for loading user details from the database.
 * 
 * This service is used by Spring Security to load user information during authentication.
 * 
 * Validates: Requirements 1, 2, 3, 26
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user details by username.
     * 
     * @param username the username
     * @return UserDetails object
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // FirebaseTokenFilter passes email as "username" — try email first, then username field
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .orElseThrow(() -> {
                    log.error("User not found with email/username: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
        log.debug("Loaded user details for: {}", username);
        return user;
    }

    /**
     * Load user details by email.
     * 
     * @param email the email address
     * @return UserDetails object
     * @throws UsernameNotFoundException if user is not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.debug("Loaded user details for email: {}", email);
        return user;
    }
}
