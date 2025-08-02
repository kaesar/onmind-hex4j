package co.onmind.microhex.application.ports.in;

import co.onmind.microhex.application.dto.in.CreateRoleRequestDto;
import co.onmind.microhex.application.dto.out.RoleResponseDto;

/**
 * Input port for creating roles.
 * 
 * This interface defines the contract for creating new roles in the system.
 * It follows the hexagonal architecture pattern by defining a port that
 * isolates the application core from external concerns.
 * 
 * The use case is responsible for orchestrating the role creation process,
 * including validation, business rule enforcement, and persistence coordination.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
public interface CreateRoleTrait {
    
    /**
     * Creates a new role in the system.
     * 
     * This method processes a role creation request by:
     * 1. Validating the input data
     * 2. Checking business rules (e.g., uniqueness)
     * 3. Creating the domain model
     * 4. Persisting the role
     * 5. Returning the created role information
     * 
     * @param request The role creation request containing the role name and other required data
     * @return RoleResponseDto containing the created role information including generated ID and timestamp
     * @throws co.onmind.microhex.domain.exceptions.DuplicateRoleException if a role with the same name already exists
     * @throws IllegalArgumentException if the request contains invalid data
     */
    RoleResponseDto createRole(CreateRoleRequestDto request);
}