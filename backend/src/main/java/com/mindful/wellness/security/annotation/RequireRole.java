package com.mindful.wellness.security.annotation;

import com.mindful.wellness.entity.UserRole;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * Custom annotation for role-based access control.
 * 
 * This annotation can be applied to controller methods to restrict access
 * to users with specific roles. It supports single or multiple roles.
 * 
 * Usage:
 * - @RequireRole(UserRole.STUDENT) - Only students can access
 * - @RequireRole(UserRole.COUNSELLOR) - Only counsellors can access
 * - @RequireRole(UserRole.ADMIN) - Only admins can access
 * 
 * Validates: Requirements 1, 2, 3, 25
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    /**
     * The required role(s) for accessing the annotated method.
     * 
     * @return array of required roles
     */
    UserRole[] value() default {};
}
