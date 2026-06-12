package com.mindful.wellness.security.exception;

/**
 * Custom exception for authentication-related errors.
 * 
 * This exception is thrown when authentication operations fail,
 * such as invalid credentials or token validation failures.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
