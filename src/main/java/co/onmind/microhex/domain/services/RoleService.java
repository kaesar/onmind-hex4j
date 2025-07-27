package co.onmind.microhex.domain.services;

import co.onmind.microhex.domain.models.Role;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain service for Role business logic.
 * 
 * This service contains the core business rules and validation logic for roles.
 * It operates on domain models and enforces business constraints independent
 * of any infrastructure concerns.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@Service
public class RoleService {
    
    /**
     * Creates a new role with the specified name.
     * 
     * This method encapsulates the business logic for role creation,
     * including validation and setting default values.
     * 
     * @param name the role name
     * @return a new Role instance
     * @throws IllegalArgumentException if the name is invalid
     */
    public Role createRole(String name) {
        validateRoleName(name);
        
        Role role = new Role();
        role.setName(name.trim());
        role.setCreatedAt(LocalDateTime.now());
        
        // Additional business logic can be added here
        validateRoleBusinessRules(role);
        
        return role;
    }
    
    /**
     * Updates an existing role with a new name.
     * 
     * @param existingRole the role to update
     * @param newName the new role name
     * @return the updated role
     * @throws IllegalArgumentException if the new name is invalid
     */
    public Role updateRole(Role existingRole, String newName) {
        if (existingRole == null) {
            throw new IllegalArgumentException("Existing role cannot be null");
        }
        
        validateRoleName(newName);
        existingRole.setName(newName.trim());
        validateRoleBusinessRules(existingRole);
        
        return existingRole;
    }
    
    /**
     * Validates role business rules.
     * 
     * This method contains domain-specific validation logic that goes beyond
     * simple field validation. It enforces business constraints and rules.
     * 
     * @param role the role to validate
     * @throws IllegalArgumentException if business rules are violated
     */
    public void validateRoleBusinessRules(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        // Validate the role name using service validation
        validateRoleName(role.getName());
        
        // Additional business rules can be added here
        validateRoleNameBusinessRules(role.getName());
        
        // Ensure the role is in a valid state
        if (!isRoleActive(role)) {
            throw new IllegalArgumentException("Role must be in an active state");
        }
    }
    
    /**
     * Validates role name according to business rules.
     * 
     * @param name the role name to validate
     * @throws IllegalArgumentException if the name violates business rules
     */
    private void validateRoleName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }
        
        String trimmedName = name.trim();
        
        if (trimmedName.length() < 2) {
            throw new IllegalArgumentException("Role name must be at least 2 characters long");
        }
        
        if (trimmedName.length() > 50) {
            throw new IllegalArgumentException("Role name cannot exceed 50 characters");
        }
        
        if (!trimmedName.matches("^[a-zA-Z0-9_\\s-]+$")) {
            throw new IllegalArgumentException("Role name can only contain letters, numbers, spaces, hyphens and underscores");
        }
    }
    
    /**
     * Validates role name according to additional business rules.
     * 
     * @param name the role name to validate
     * @throws IllegalArgumentException if the name violates business rules
     */
    private void validateRoleNameBusinessRules(String name) {
        // Check for reserved role names
        List<String> reservedNames = List.of("SYSTEM", "ROOT", "NULL", "UNDEFINED");
        
        if (reservedNames.contains(name.toUpperCase())) {
            throw new IllegalArgumentException("Role name '" + name + "' is reserved and cannot be used");
        }
        
        // Check for inappropriate prefixes
        if (name.toUpperCase().startsWith("SYS_") || name.toUpperCase().startsWith("INTERNAL_")) {
            throw new IllegalArgumentException("Role name cannot start with system prefixes (SYS_, INTERNAL_)");
        }
        
        // Additional business rules can be added here
    }
    
    /**
     * Validates that a role can be deleted according to business rules.
     * 
     * @param role the role to validate for deletion
     * @throws IllegalArgumentException if the role cannot be deleted
     */
    public void validateRoleDeletion(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        // Business rule: Cannot delete system roles
        if (role.getName() != null && role.getName().toUpperCase().contains("SYSTEM")) {
            throw new IllegalArgumentException("System roles cannot be deleted");
        }
        
        // Additional deletion validation rules can be added here
    }
    
    /**
     * Checks if a role name is valid according to business rules.
     * 
     * @param name the role name to check
     * @return true if the name is valid, false otherwise
     */
    public boolean isValidRoleName(String name) {
        try {
            validateRoleName(name);
            validateRoleNameBusinessRules(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Normalizes a role name according to business rules.
     * 
     * @param name the role name to normalize
     * @return the normalized role name
     */
    public String normalizeRoleName(String name) {
        if (name == null) {
            return null;
        }
        
        // Trim whitespace and convert to proper case
        String normalized = name.trim();
        
        // Replace multiple spaces with single space
        normalized = normalized.replaceAll("\\s+", " ");
        
        return normalized;
    }
    
    /**
     * Checks if a role is considered active.
     * A role is active if it has been created and has a valid name.
     * 
     * @param role the role to check
     * @return true if the role is active, false otherwise
     */
    public boolean isRoleActive(Role role) {
        return role != null && 
               role.getName() != null && 
               !role.getName().trim().isEmpty() && 
               role.getCreatedAt() != null;
    }
}