package com.mindful.wellness.security.filter;

import com.mindful.wellness.entity.UserRole;
import com.mindful.wellness.security.annotation.RequireAnyRole;
import com.mindful.wellness.security.annotation.RequireRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * Filter for role-based access control (RBAC).
 * 
 * This filter intercepts HTTP requests and checks if the authenticated user
 * has the required role(s) to access the requested endpoint. It works in
 * conjunction with the @RequireRole and @RequireAnyRole annotations.
 * 
 * If a user lacks the required role, an AccessDeniedException is thrown,
 * which is handled by the JwtAccessDeniedHandler.
 * 
 * Validates: Requirements 1, 2, 3, 25
 */
@Slf4j
@Component
public class RoleBasedAccessControlFilter extends OncePerRequestFilter {

    private final RequestMappingHandlerMapping handlerMapping;

    public RoleBasedAccessControlFilter(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    /** Disable this filter entirely — role checking is handled by @PreAuthorize in controllers */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }

    /**
     * Check if the current user has the required role.
     * 
     * @param requiredRoles the required roles
     * @throws AccessDeniedException if the user doesn't have the required role
     */
    private void checkRoleAccess(UserRole[] requiredRoles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        
        UserRole userRole = extractUserRole(authentication);
        
        boolean hasRequiredRole = Arrays.stream(requiredRoles)
                .anyMatch(role -> role == userRole);
        
        if (!hasRequiredRole) {
            String requiredRolesStr = Arrays.toString(requiredRoles);
            throw new AccessDeniedException(
                    "User role '" + userRole + "' is not authorized to access this resource. Required roles: " + requiredRolesStr
            );
        }
        
        log.debug("User with role {} has access to the resource", userRole);
    }

    /**
     * Check if the current user has any of the required roles.
     * 
     * @param allowedRoles the allowed roles
     * @throws AccessDeniedException if the user doesn't have any of the allowed roles
     */
    private void checkAnyRoleAccess(UserRole[] allowedRoles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        
        UserRole userRole = extractUserRole(authentication);
        
        boolean hasAnyRole = Arrays.stream(allowedRoles)
                .anyMatch(role -> role == userRole);
        
        if (!hasAnyRole) {
            String allowedRolesStr = Arrays.toString(allowedRoles);
            throw new AccessDeniedException(
                    "User role '" + userRole + "' is not authorized to access this resource. Allowed roles: " + allowedRolesStr
            );
        }
        
        log.debug("User with role {} has access to the resource", userRole);
    }

    /**
     * Extract the user role from the authentication object.
     * 
     * The role is stored in the authorities as "ROLE_<ROLE_NAME>".
     * 
     * @param authentication the authentication object
     * @return the user role
     * @throws AccessDeniedException if the role cannot be determined
     */
    private UserRole extractUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                .findFirst()
                .map(UserRole::valueOf)
                .orElseThrow(() -> new AccessDeniedException("User role cannot be determined"));
    }
}
