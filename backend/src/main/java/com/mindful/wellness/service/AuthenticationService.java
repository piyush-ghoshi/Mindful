package com.mindful.wellness.service;

import com.mindful.wellness.dto.*;
import com.mindful.wellness.entity.User;
import com.mindful.wellness.entity.UserRole;
import com.mindful.wellness.repository.UserRepository;
import com.mindful.wellness.security.exception.AuthenticationException;
import com.mindful.wellness.security.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Service for handling user authentication including registration and login.
 * 
 * This service manages:
 * - User registration with email validation and duplicate checking
 * - User login with credential verification
 * - Password hashing using BCrypt
 * - JWT token generation and refresh
 * - Password validation against complexity requirements
 * 
 * Validates: Requirements 1, 2, 3, 25, 26
 */
@Slf4j
@Service
@Transactional
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpirationMs;

    /**
     * Password complexity regex pattern.
     * Requires: at least 8 characters, 1 uppercase, 1 lowercase, 1 number, 1 special character
     */
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    /**
     * Register a new user.
     * 
     * Validates email uniqueness, password complexity, and creates a new user account.
     * 
     * @param registerRequest the registration request containing user data
     * @return AuthResponse with user information and tokens
     * @throws AuthenticationException if registration fails
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Attempting to register user with email: {}", registerRequest.getEmail());

        // Validate password complexity
        if (!validatePassword(registerRequest.getPassword())) {
            log.warn("Password does not meet complexity requirements for email: {}", registerRequest.getEmail());
            throw new AuthenticationException(
                "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character"
            );
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Registration attempt with existing email: {}", registerRequest.getEmail());
            throw new AuthenticationException("Email is already registered");
        }

        // Create new user
        User user = User.builder()
                .email(registerRequest.getEmail())
                .username(registerRequest.getEmail()) // Use email as username
                .passwordHash(hashPassword(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phoneNumber(registerRequest.getPhoneNumber())
                .languagePreference(registerRequest.getLanguagePreference() != null ? 
                    registerRequest.getLanguagePreference() : "en")
                .role(UserRole.STUDENT)
                .isActive(true)
                .isEmailVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with email: {}", user.getEmail());

        // Generate tokens
        TokenPair tokenPair = generateTokens(user);

        // Build response
        return AuthResponse.builder()
                .user(convertToUserDto(user))
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .expiresIn(tokenPair.getExpiresIn())
                .tokenType("Bearer")
                .message("User registered successfully")
                .build();
    }

    /**
     * Authenticate user and generate tokens.
     * 
     * Verifies user credentials and generates JWT tokens on successful authentication.
     * 
     * @param loginRequest the login request containing email and password
     * @return AuthResponse with user information and tokens
     * @throws AuthenticationException if authentication fails
     */
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Attempting to login user with email: {}", loginRequest.getEmail());

        try {
            // Find user by email
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

            // Verify password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.warn("Invalid password attempt for email: {}", loginRequest.getEmail());
                throw new AuthenticationException("Invalid email or password");
            }

            // Check if user is active
            if (!user.getIsActive()) {
                log.warn("Login attempt for inactive user: {}", loginRequest.getEmail());
                throw new AuthenticationException("User account is inactive");
            }

            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Update last login time
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            TokenPair tokenPair = generateTokens(user);

            log.info("User logged in successfully: {}", user.getEmail());

            // Build response
            return AuthResponse.builder()
                    .user(convertToUserDto(user))
                    .accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken())
                    .expiresIn(tokenPair.getExpiresIn())
                    .tokenType("Bearer")
                    .message("Login successful")
                    .build();

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            throw new AuthenticationException("Authentication failed");
        }
    }

    /**
     * Validate password complexity.
     * 
     * Password must contain:
     * - At least 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one number
     * - At least one special character (@$!%*?&)
     * 
     * @param password the password to validate
     * @return true if password meets complexity requirements, false otherwise
     */
    public boolean validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return pattern.matcher(password).matches();
    }

    /**
     * Hash password using BCrypt.
     * 
     * @param password the plain text password
     * @return the hashed password
     */
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Generate JWT token pair (access token and refresh token).
     * 
     * @param user the user for whom to generate tokens
     * @return TokenPair containing access and refresh tokens
     */
    public TokenPair generateTokens(User user) {
        String accessToken = jwtTokenProvider.generateTokenFromUsername(user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshTokenFromUsername(user.getUsername());

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtExpirationMs / 1000) // Convert to seconds
                .tokenType("Bearer")
                .build();
    }

    /**
     * Validate refresh token.
     * 
     * @param refreshToken the refresh token to validate
     * @return true if refresh token is valid, false otherwise
     */
    public boolean validateRefreshToken(String refreshToken) {
        return jwtTokenProvider.validateToken(refreshToken);
    }

    /**
     * Get username from refresh token.
     * 
     * @param refreshToken the refresh token
     * @return the username encoded in the token
     */
    public String getUsernameFromRefreshToken(String refreshToken) {
        return jwtTokenProvider.getUsernameFromToken(refreshToken);
    }

    /**
     * Generate new access token from username.
     * 
     * @param username the username
     * @return the new access token
     */
    public String generateAccessToken(String username) {
        return jwtTokenProvider.generateTokenFromUsername(username);
    }

    /**
     * Convert User entity to UserDto.
     * 
     * @param user the user entity
     * @return UserDto with non-sensitive user information
     */
    private UserDto convertToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .languagePreference(user.getLanguagePreference())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
