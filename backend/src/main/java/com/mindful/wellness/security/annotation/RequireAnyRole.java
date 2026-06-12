package com.mindful.wellness.security.annotation;

import com.mindful.wellness.entity.UserRole;

import java.lang.annotation.*;

/**
 * Custom annotation for role-based access control with multiple roles.
 * 
 * This annotation can be applied to controller methods to restrict access
 * to users with any of the specified roles.
 * 
 * Usage:
 * - @RequireAnyRole({UserRole.STUDENT, UserRole.COUNSELLOR}) - Students or counsellors can access
 * - @RequireAnyRole({UserRole.COUNSELLOR, UserRole.ADMIN}) - Counsellors or admins can access
 * 
 * Validates: Requirements 1, 2, 3, 25
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAnyRole {
    /**
     * The allowed role(s) for accessing the annotated method.
     * 
     * @return array of allowed roles
     */
    UserRole[] value() default {};
}
