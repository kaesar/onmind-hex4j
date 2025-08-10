package co.onmind.microhex.domain.ports.in;

import co.onmind.microhex.domain.models.Role;

import java.util.List;
import java.util.Optional;

/**
 * Input port for role operations.
 * This interface defines the contract for role business operations
 * and is implemented by the domain service.
 */
public interface RoleServicePort {
    
    // ========== COMMANDS (Write Operations) ==========
    
    /**
     * Creates a new role in the system.
     * @param name the role name
     * @return the created role
     */
    Role createRole(String name);
    
    /**
     * Updates an existing role's name.
     * @param id the role ID
     * @param newName the new role name
     * @return the updated role
     */
    Role updateRole(Long id, String newName);
    
    /**
     * Deletes a role from the system.
     * @param id the role ID to delete
     */
    void deleteRole(Long id);
    
    // ========== QUERIES (Read Operations) ==========
    
    /**
     * Retrieves a role by its ID.
     * @param id the role ID
     * @return the role if found
     */
    Optional<Role> getRoleById(Long id);
    
    /**
     * Retrieves a role by its name.
     * @param name the role name
     * @return the role if found
     */
    Optional<Role> getRoleByName(String name);
    
    /**
     * Retrieves all roles in the system.
     * @return list of all roles
     */
    List<Role> getAllRoles();
    
    /**
     * Searches roles by name pattern.
     * @param pattern the search pattern
     * @return list of matching roles
     */
    List<Role> searchRolesByName(String pattern);
    
    /**
     * Gets the total count of roles.
     * @return the total count
     */
    Long getRoleCount();
    
    /**
     * Checks if a role exists by name.
     * @param name the role name
     * @return true if exists, false otherwise
     */
    boolean roleExists(String name);
}