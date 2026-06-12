package com.mindful.wellness.repository;

import com.mindful.wellness.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Role entity.
 * 
 * Provides database operations for Role entities including
 * CRUD operations and custom queries.
 * 
 * Validates: Requirements 1, 2, 3, 25
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    /**
     * Find a role by its name.
     * 
     * @param name the role name
     * @return an Optional containing the role if found
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if a role exists by name.
     * 
     * @param name the role name
     * @return true if the role exists, false otherwise
     */
    boolean existsByName(String name);
}
