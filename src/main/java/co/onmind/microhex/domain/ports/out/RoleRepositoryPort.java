package co.onmind.microhex.domain.ports.out;

import co.onmind.microhex.domain.models.Role;

import java.util.List;
import java.util.Optional;

/**
 * Output port for role repository operations.
 * This interface defines the contract for role persistence operations
 * and is implemented by infrastructure adapters.
 */
public interface RoleRepositoryPort {
    
    /**
     * Saves a role to the repository.
     * @param role the role to save
     * @return the saved role with generated ID
     */
    Role save(Role role);
    
    /**
     * Finds a role by its ID.
     * @param id the role ID
     * @return the role if found
     */
    Optional<Role> findById(Long id);
    
    /**
     * Finds a role by its name.
     * @param name the role name
     * @return the role if found
     */
    Optional<Role> findByName(String name);
    
    /**
     * Finds all roles.
     * @return list of all roles
     */
    List<Role> findAll();
    
    /**
     * Finds roles by name pattern.
     * @param pattern the search pattern
     * @return list of matching roles
     */
    List<Role> findByNameContaining(String pattern);
    
    /**
     * Checks if a role exists by name.
     * @param name the role name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Deletes a role by its ID.
     * @param id the role ID
     * @return true if deleted, false if not found
     */
    boolean deleteById(Long id);
    
    /**
     * Counts the total number of roles.
     * @return the total count
     */
    Long count();
}