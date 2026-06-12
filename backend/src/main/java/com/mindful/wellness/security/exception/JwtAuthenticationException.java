package com.mindful.wellness.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Custom exception for JWT-related authentication errors.
 * 
 * This exception is thrown when JWT token validation or processing fails.
 */
public class JwtAuthenticationException extends AuthenticationException {

    public JwtAuthenticationException(String message) {
        super(message);
    }

    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
