# JWT Authentication Infrastructure

## Overview

This document describes the JWT (JSON Web Token) authentication infrastructure implemented for the Mindful Wellness Platform backend. The implementation provides secure, stateless authentication using JWT tokens with Spring Security integration.

## Components

### 1. JWT Token Provider (`JwtTokenProvider.java`)

**Location**: `com.mindful.wellness.security.jwt`

**Responsibilities**:
- Generate JWT tokens from authentication objects or usernames
- Validate JWT token signatures and expiration
- Extract username from JWT tokens
- Check token expiration status

**Key Methods**:
- `generateToken(Authentication)`: Creates a JWT token from an authenticated user
- `generateTokenFromUsername(String)`: Creates a JWT token from a username string
- `validateToken(String)`: Validates token signature and format
- `getUsernameFromToken(String)`: Extracts the username from a token
- `isTokenExpired(String)`: Checks if a token has expired

**Configuration**:
- Uses HS512 (HMAC with SHA-512) for signing
- Token expiration: 24 hours (86400000 ms) - configurable via `jwt.expiration`
- Secret key: Configured via `jwt.secret` property (minimum 256 bits recommended)

### 2. JWT Authentication Filter (`JwtAuthenticationFilter.java`)

**Location**: `com.mindful.wellness.security.jwt`

**Responsibilities**:
- Intercepts HTTP requests to extract JWT tokens
- Validates tokens and loads user details
- Sets authentication in Spring Security context
- Handles authentication errors gracefully

**Token Extraction**:
- Expects tokens in the `Authorization` header
- Format: `Bearer <token>`
- Automatically skips requests without tokens

**Filter Chain Integration**:
- Registered before `UsernamePasswordAuthenticationFilter`
- Runs once per request
- Non-blocking error handling

### 3. User Entity (`User.java`)

**Location**: `com.mindful.wellness.entity`

**Implements**: `UserDetails` (Spring Security interface)

**Fields**:
- `id`: UUID primary key
- `username`: Unique username for login
- `email`: Unique email address
- `password`: BCrypt-hashed password
- `role`: User role (STUDENT, COUNSELLOR, ADMIN)
- `isActive`: Account activation status
- `isEmailVerified`: Email verification status
- `languagePreference`: Preferred language (default: "en")
- `createdAt`, `updatedAt`, `lastLoginAt`: Timestamps

**Indexes**:
- `idx_email`: Unique index on email
- `idx_username`: Unique index on username
- `idx_role`: Index on role for filtering
- `idx_is_active`: Index on active status

### 4. User Role Enum (`UserRole.java`)

**Location**: `com.mindful.wellness.entity`

**Roles**:
- `STUDENT`: Students seeking wellness support
- `COUNSELLOR`: Licensed professionals providing counseling
- `ADMIN`: Administrators managing the platform

### 5. User Repository (`UserRepository.java`)

**Location**: `com.mindful.wellness.repository`

**Query Methods**:
- `findByUsername(String)`: Find user by username
- `findByEmail(String)`: Find user by email
- `existsByUsername(String)`: Check username existence
- `existsByEmail(String)`: Check email existence
- `findByRole(UserRole)`: Find all users with a specific role
- `findActiveByRole(UserRole)`: Find active users with a specific role
- `findAllActive()`: Find all active users
- `findAllInactive()`: Find all inactive users

### 6. Custom User Details Service (`CustomUserDetailsService.java`)

**Location**: `com.mindful.wellness.security.service`

**Implements**: `UserDetailsService` (Spring Security interface)

**Methods**:
- `loadUserByUsername(String)`: Load user by username (required by Spring Security)
- `loadUserByEmail(String)`: Load user by email (custom method)

**Error Handling**:
- Throws `UsernameNotFoundException` if user not found
- Logs errors for debugging

### 7. Security Configuration (`SecurityConfig.java`)

**Location**: `com.mindful.wellness.config`

**Configuration**:
- **CSRF**: Disabled for stateless API
- **CORS**: Configured for localhost:3000 and localhost:5173
- **Session Management**: Stateless (STATELESS policy)
- **Password Encoding**: BCrypt with strength 12
- **Authentication Manager**: Configured for authentication

**Public Endpoints**:
- `POST /api/auth/register`: User registration
- `POST /api/auth/login`: User login
- `POST /api/auth/forgot-password`: Password reset request
- `POST /api/auth/reset-password`: Password reset confirmation
- `GET /api/health`: Health check
- `GET /api/public/**`: Public resources
- `GET /api/crisis/**`: Crisis support resources

**Protected Endpoints**:
- All other endpoints require authentication

**Exception Handling**:
- `JwtAuthenticationEntryPoint`: Handles unauthenticated requests
- `JwtAccessDeniedHandler`: Handles authorization failures

### 8. Authentication Entry Point (`JwtAuthenticationEntryPoint.java`)

**Location**: `com.mindful.wellness.security.handler`

**Responsibility**: Handle unauthenticated requests

**Response Format**:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/protected-endpoint"
}
```

### 9. Access Denied Handler (`JwtAccessDeniedHandler.java`)

**Location**: `com.mindful.wellness.security.handler`

**Responsibility**: Handle authorization failures (insufficient permissions)

**Response Format**:
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource",
  "path": "/api/admin-endpoint"
}
```

### 10. Custom Exceptions

**Location**: `com.mindful.wellness.security.exception`

- `AuthenticationException`: General authentication errors
- `JwtAuthenticationException`: JWT-specific authentication errors

## Database Schema

### Users Table

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    profile_picture_url VARCHAR(500),
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_email_verified BOOLEAN NOT NULL DEFAULT false,
    language_preference VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP
);
```

**Indexes**:
- `idx_email`: Unique index on email
- `idx_username`: Unique index on username
- `idx_role`: Index on role
- `idx_is_active`: Index on is_active
- `idx_created_at`: Index on created_at

## Configuration Properties

### JWT Configuration

```properties
# JWT secret key (minimum 256 bits for HS512)
jwt.secret=your-secret-key-change-this-in-production-with-a-strong-key-at-least-256-bits-minimum-32-bytes

# Token expiration time in milliseconds (24 hours)
jwt.expiration=86400000

# Refresh token expiration (7 days)
jwt.refresh-expiration=604800000
```

### Security Configuration

```properties
# CORS allowed origins
security.cors.allowed-origins=http://localhost:3000,http://localhost:5173

# CORS allowed methods
security.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS,PATCH

# CORS allowed headers
security.cors.allowed-headers=*

# CORS max age
security.cors.max-age=3600

# CORS allow credentials
security.cors.allow-credentials=true

# Session timeout (30 minutes)
security.session.timeout=1800

# Max concurrent sessions per user
security.session.max-concurrent-sessions=1
```

## Authentication Flow

### 1. User Registration

```
POST /api/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "firstName": "John",
  "lastName": "Doe",
  "role": "STUDENT"
}

Response:
{
  "id": "uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "STUDENT",
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### 2. User Login

```
POST /api/auth/login
{
  "username": "john_doe",
  "password": "SecurePassword123!"
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": "uuid",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "STUDENT"
  }
}
```

### 3. Authenticated Request

```
GET /api/user/profile
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

Response:
{
  "id": "uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "STUDENT"
}
```

## Security Features

### 1. Password Hashing

- **Algorithm**: BCrypt with strength 12
- **Automatic**: Applied during user registration
- **Verification**: Automatic during login

### 2. Token Security

- **Algorithm**: HS512 (HMAC with SHA-512)
- **Expiration**: 24 hours (configurable)
- **Validation**: Signature and expiration checked on every request
- **Storage**: Stateless (no server-side storage)

### 3. CORS Configuration

- **Allowed Origins**: localhost:3000, localhost:5173
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS, PATCH
- **Credentials**: Allowed
- **Max Age**: 3600 seconds

### 4. Session Management

- **Type**: Stateless (JWT-based)
- **Timeout**: 30 minutes of inactivity
- **Concurrent Sessions**: 1 per user (prevents multiple logins)

### 5. Role-Based Access Control

- **Roles**: STUDENT, COUNSELLOR, ADMIN
- **Enforcement**: Via Spring Security annotations
- **Granularity**: Method-level and endpoint-level

## Error Handling

### Authentication Errors

**401 Unauthorized**:
- Missing or invalid token
- Expired token
- Invalid credentials

**403 Forbidden**:
- Insufficient permissions
- Role-based access denied

### Response Format

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/api/protected-endpoint"
}
```

## Testing

### Unit Tests

Test the following scenarios:
- Token generation and validation
- User authentication with valid/invalid credentials
- Token expiration
- User details loading
- Role-based access control

### Integration Tests

Test the following flows:
- Complete registration and login flow
- Protected endpoint access with valid token
- Protected endpoint access without token
- Protected endpoint access with invalid token
- Role-based endpoint access

## Production Considerations

### 1. Secret Key Management

- **Never hardcode** the JWT secret in code
- **Use environment variables** or external configuration
- **Rotate keys** periodically
- **Minimum length**: 256 bits (32 bytes) for HS512

### 2. HTTPS/TLS

- **Always use HTTPS** in production
- **Configure SSL certificates**
- **Enable HSTS** headers

### 3. Token Expiration

- **Short expiration** (24 hours) for security
- **Implement refresh tokens** for better UX
- **Revocation mechanism** for logout

### 4. Rate Limiting

- **Implement rate limiting** on authentication endpoints
- **Prevent brute force attacks**
- **Log failed attempts**

### 5. Monitoring and Logging

- **Log all authentication events**
- **Monitor failed login attempts**
- **Alert on suspicious activity**
- **Audit trail** for compliance

## Troubleshooting

### Common Issues

1. **"Invalid JWT token"**
   - Check token format (should start with "Bearer ")
   - Verify token hasn't expired
   - Ensure secret key matches

2. **"User not found"**
   - Verify username/email exists in database
   - Check for typos in credentials
   - Ensure user account is active

3. **"Unauthorized" on protected endpoints**
   - Verify token is included in Authorization header
   - Check token expiration
   - Ensure user has required role

4. **CORS errors**
   - Verify frontend origin is in allowed origins
   - Check CORS configuration in SecurityConfig
   - Ensure credentials are properly configured

## References

- [JWT.io](https://jwt.io/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JJWT Library](https://github.com/jwtk/jjwt)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

## Requirements Validation

This implementation validates the following requirements:

- **Requirement 1**: Student Authentication and Registration
- **Requirement 2**: Counselor Authentication
- **Requirement 3**: Administrator Authentication
- **Requirement 25**: Data Privacy and Security
- **Requirement 26**: Session Management

## Next Steps

1. Implement authentication endpoints (login, register, password reset)
2. Create DTOs for authentication requests/responses
3. Implement user service for user management
4. Add email verification functionality
5. Implement password reset workflow
6. Add rate limiting for authentication endpoints
7. Implement refresh token mechanism
8. Add comprehensive logging and monitoring
