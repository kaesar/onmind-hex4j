package co.onmind.microhex.infrastructure.controllers;

import co.onmind.microhex.application.dto.in.CreateRoleRequestDto;
import co.onmind.microhex.application.dto.out.RoleResponseDto;
import co.onmind.microhex.application.ports.in.CreateRoleUseCase;
import co.onmind.microhex.application.ports.in.GetRoleUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for role management operations.
 * 
 * This controller provides HTTP endpoints for creating and retrieving roles.
 * It follows REST conventions and integrates with the hexagonal architecture
 * through input ports (use cases).
 * 
 * All endpoints include proper validation, error handling, and logging.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    
    private final CreateRoleUseCase createRoleUseCase;
    private final GetRoleUseCase getRoleUseCase;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param createRoleUseCase Use case for creating roles
     * @param getRoleUseCase Use case for retrieving roles
     */
    public RoleController(CreateRoleUseCase createRoleUseCase, GetRoleUseCase getRoleUseCase) {
        this.createRoleUseCase = createRoleUseCase;
        this.getRoleUseCase = getRoleUseCase;
    }
    
    /**
     * Creates a new role.
     * 
     * POST /api/v1/roles
     * 
     * @param request The role creation request with validation
     * @return ResponseEntity with the created role and HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody CreateRoleRequestDto request) {
        logger.info("Creating new role with name: {}", request.name());
        
        RoleResponseDto createdRole = createRoleUseCase.createRole(request);
        
        logger.info("Successfully created role with ID: {}", createdRole.id());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }
    
    /**
     * Retrieves all roles.
     * 
     * GET /api/v1/roles
     * 
     * @return ResponseEntity with list of all roles and HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        logger.info("Retrieving all roles");
        
        List<RoleResponseDto> roles = getRoleUseCase.getAllRoles();
        
        logger.info("Successfully retrieved {} roles", roles.size());
        
        return ResponseEntity.ok(roles);
    }
    
    /**
     * Retrieves a specific role by ID.
     * 
     * GET /api/v1/roles/{id}
     * 
     * @param id The unique identifier of the role to retrieve
     * @return ResponseEntity with the role and HTTP 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable Long id) {
        logger.info("Retrieving role with ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Role ID must be a positive number");
        }
        
        RoleResponseDto role = getRoleUseCase.getRoleById(id);
        
        logger.info("Successfully retrieved role: {}", role.name());
        
        return ResponseEntity.ok(role);
    }
}