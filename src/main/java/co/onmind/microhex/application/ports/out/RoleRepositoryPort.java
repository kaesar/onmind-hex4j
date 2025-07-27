package co.onmind.microhex.application.ports.out;

import co.onmind.microhex.domain.models.Role;
import java.util.List;
import java.util.Optional;

/**
 * Output port for role persistence operations.
 * 
 * This interface defines the contract for role data persistence in the hexagonal architecture.
 * It serves as an abstraction layer between the application core and the persistence infrastructure,
 * allowing the domain logic to remain independent of specific database technologies.
 * 
 * Implementations of this port will handle the actual persistence mechanism,
 * whether it's JPA, MongoDB, or any other storage solution.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
public interface RoleRepositoryPort {
    
    /**
     * Persists a role entity to the storage system.
     * 
     * This method saves a role to the persistent storage. If the role has no ID,
     * it will be created as a new entity. If it has an ID, it will be updated.
     * 
     * @param role The role entity to be saved
     * @return The saved role entity with updated information (e.g., generated ID, timestamps)
     * @throws IllegalArgumentException if the role is null or contains invalid data
     */
    Role save(Role role);
    
    /**
     * Retrieves a role by its unique identifier.
     * 
     * This method searches for a role with the specified ID in the storage system.
     * 
     * @param id The unique identifier of the role to retrieve
     * @return Optional containing the role if found, empty Optional otherwise
     * @throws IllegalArgumentException if the ID is null
     */
    Optional<Role> findById(Long id);
    
    /**
     * Retrieves all roles from the storage system.
     * 
     * This method fetches all role entities available in the persistent storage.
     * The returned list may be empty if no roles exist.
     * 
     * @return List of all role entities in the system
     */
    List<Role> findAll();
    
    /**
     * Checks if a role exists with the specified name.
     * 
     * This method verifies the existence of a role with the given name
     * without retrieving the full entity, which is more efficient for
     * validation purposes.
     * 
     * @param name The name to check for existence
     * @return true if a role with the given name exists, false otherwise
     * @throws IllegalArgumentException if the name is null or blank
     */
    boolean existsByName(String name);
    
    /**
     * Retrieves a role by its name.
     * 
     * This method searches for a role with the specified name in the storage system.
     * This is useful for business logic that needs to work with roles by name.
     * 
     * @param name The name of the role to retrieve
     * @return Optional containing the role if found, empty Optional otherwise
     * @throws IllegalArgumentException if the name is null or blank
     */
    Optional<Role> findByName(String name);
    
    /**
     * Deletes a role by its unique identifier.
     * 
     * This method removes a role from the persistent storage system.
     * 
     * @param id The unique identifier of the role to delete
     * @throws IllegalArgumentException if the ID is null
     */
    void deleteById(Long id);
    
    /**
     * Counts the total number of roles in the system.
     * 
     * This method returns the total count of role entities in the storage system.
     * 
     * @return The total number of roles
     */
    long count();
}