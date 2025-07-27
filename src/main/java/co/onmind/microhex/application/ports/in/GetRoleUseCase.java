package co.onmind.microhex.application.ports.in;

import co.onmind.microhex.application.dto.out.RoleResponseDto;
import java.util.List;

/**
 * Input port for querying roles.
 * 
 * This interface defines the contract for retrieving role information from the system.
 * It follows the hexagonal architecture pattern by defining a port that
 * isolates the application core from external concerns.
 * 
 * The use case provides different query operations for roles, including
 * retrieving individual roles by ID and listing all available roles.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
public interface GetRoleUseCase {
    
    /**
     * Retrieves a specific role by its unique identifier.
     * 
     * This method fetches a role from the system using its ID and returns
     * the role information in a format suitable for external consumption.
     * 
     * @param id The unique identifier of the role to retrieve
     * @return RoleResponseDto containing the role information
     * @throws co.onmind.microhex.domain.exceptions.RoleNotFoundException if no role exists with the given ID
     * @throws IllegalArgumentException if the provided ID is null or invalid
     */
    RoleResponseDto getRoleById(Long id);
    
    /**
     * Retrieves all roles available in the system.
     * 
     * This method fetches all roles from the system and returns them
     * as a list of response DTOs. The list may be empty if no roles exist.
     * 
     * @return List of RoleResponseDto containing all roles in the system
     */
    List<RoleResponseDto> getAllRoles();
    
    /**
     * Checks if a role exists with the given name.
     * 
     * This method verifies the existence of a role without retrieving
     * the full role data, which is useful for validation purposes.
     * 
     * @param name The name of the role to check for existence
     * @return true if a role with the given name exists, false otherwise
     * @throws IllegalArgumentException if the provided name is null or blank
     */
    boolean existsByName(String name);
}