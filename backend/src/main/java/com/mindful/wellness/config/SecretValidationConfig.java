package com.mindful.wellness.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Profile;

/**
 * Fail-fast validation of critical secrets at startup.
 *
 * Ensures the application does not start with missing or placeholder
 * credentials — preventing accidental deployment with insecure defaults.
 */
@Configuration
@Profile("!test")
@Slf4j
public class SecretValidationConfig {

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${firebase.credentials.path:}")
    private String firebaseCredentialsPath;

    @PostConstruct
    public void validateSecrets() {
        boolean fatal = false;

        if (isBlank(dbPassword)) {
            log.error("FATAL: DATABASE_PASSWORD is not set. Set it via environment variable.");
            fatal = true;
        }

        if (isBlank(jwtSecret)) {
            log.error("FATAL: JWT_SECRET is not set. Set it via environment variable (min 32 chars).");
            fatal = true;
        } else if (jwtSecret.length() < 32) {
            log.error("FATAL: JWT_SECRET is too short ({}). Must be at least 32 characters (256 bits).", jwtSecret.length());
            fatal = true;
        }

        if (isBlank(groqApiKey)) {
            log.warn("WARNING: GROQ_API_KEY is not set. MindBot AI chat will use fallback responses.");
        }

        // Firebase can come from env var FIREBASE_CREDENTIALS_JSON or file path
        String firebaseJson = System.getenv("FIREBASE_CREDENTIALS_JSON");
        if (isBlank(firebaseCredentialsPath) && isBlank(firebaseJson)) {
            log.warn("WARNING: No Firebase credentials configured. Set FIREBASE_CREDENTIALS_JSON or FIREBASE_CREDENTIALS_PATH.");
        }

        if (fatal) {
            throw new IllegalStateException(
                    "Application startup aborted: critical secrets are missing. " +
                    "See logs above and configure via environment variables. " +
                    "Refer to backend/.env.example for required variables.");
        }

        log.info("Secret validation passed — all critical credentials are configured.");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
