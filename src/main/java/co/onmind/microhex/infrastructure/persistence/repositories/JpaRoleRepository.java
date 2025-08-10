package co.onmind.microhex.infrastructure.persistence.repositories;

import co.onmind.microhex.infrastructure.persistence.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository interface for Role entity persistence operations.
 * 
 * This interface extends JpaRepository to provide standard CRUD operations
 * and defines custom query methods for role-specific operations.
 * Spring Data JPA will automatically provide implementations for these methods.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@Repository
public interface JpaRoleRepository extends JpaRepository<RoleEntity, Long> {
    
    /**
     * Finds a role entity by its name.
     * 
     * @param name The name of the role to find
     * @return Optional containing the role entity if found, empty otherwise
     */
    Optional<RoleEntity> findByName(String name);
    
    /**
     * Checks if a role exists with the given name.
     * 
     * @param name The name to check for existence
     * @return true if a role with the name exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Finds a role by name ignoring case.
     * This method provides case-insensitive search functionality.
     * 
     * @param name The name of the role to find (case-insensitive)
     * @return Optional containing the role entity if found, empty otherwise
     */
    Optional<RoleEntity> findByNameIgnoreCase(String name);
    
    /**
     * Checks if a role exists with the given name (case-insensitive).
     * 
     * @param name The name to check for existence (case-insensitive)
     * @return true if a role with the name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Custom query to find roles by name containing a specific substring.
     * This demonstrates how to use custom JPQL queries when needed.
     * 
     * @param namePattern The pattern to search for in role names
     * @return List of role entities matching the pattern
     */
    @Query("SELECT r FROM RoleEntity r WHERE r.name LIKE %:namePattern%")
    java.util.List<RoleEntity> findByNameContaining(@Param("namePattern") String namePattern);
    
    /**
     * Finds roles by name containing a specific substring (case-insensitive).
     * 
     * @param namePattern The pattern to search for in role names
     * @return List of role entities matching the pattern
     */
    java.util.List<RoleEntity> findByNameContainingIgnoreCase(String namePattern);
    
    /**
     * Custom query to count roles created after a specific date.
     * This demonstrates more complex custom queries.
     * 
     * @param date The date to compare against
     * @return Count of roles created after the specified date
     */
    @Query("SELECT COUNT(r) FROM RoleEntity r WHERE r.createdAt > :date")
    long countRolesCreatedAfter(@Param("date") java.time.LocalDateTime date);
}