package com.mindful.wellness.repository;

import com.mindful.wellness.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Permission entity.
 * 
 * Provides database operations for Permission entities including
 * CRUD operations and custom queries.
 * 
 * Validates: Requirements 1, 2, 3, 25
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    
    /**
     * Find a permission by its name.
     * 
     * @param name the permission name
     * @return an Optional containing the permission if found
     */
    Optional<Permission> findByName(String name);
    
    /**
     * Check if a permission exists by name.
     * 
     * @param name the permission name
     * @return true if the permission exists, false otherwise
     */
    boolean existsByName(String name);
}
