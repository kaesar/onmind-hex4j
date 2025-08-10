package co.onmind.microhex.infrastructure.controllers;

import co.onmind.microhex.application.dto.CreateRoleRequest;
import co.onmind.microhex.application.dto.RoleResponse;
import co.onmind.microhex.application.dto.UpdateRoleRequest;
import co.onmind.microhex.application.handlers.RoleHandler;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for role management operations.
 * 
 * This controller provides HTTP endpoints for role CRUD operations.
 * It follows REST conventions and integrates with the hexagonal architecture
 * through application handlers that orchestrate domain operations.
 * 
 * All endpoints include proper validation, error handling, and logging.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    
    private final RoleHandler roleHandler;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param roleHandler Application handler for role operations
     */
    public RoleController(RoleHandler roleHandler) {
        this.roleHandler = roleHandler;
    }
    
    /**
     * Creates a new role.
     * 
     * POST /api/v1/roles
     * 
     * @param request The role creation request with validation
     * @return ResponseEntity with the created role and appropriate HTTP status
     */
    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        logger.info("Creating new role with name: {}", request.getName());
        
        ResponseEntity<RoleResponse> response = roleHandler.createRole(request);
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            logger.info("Successfully created role with ID: {}", response.getBody().getId());
        }
        
        return response;
    }
    
    /**
     * Updates an existing role.
     * 
     * PUT /api/v1/roles/{id}
     * 
     * @param id The role ID to update
     * @param request The role update request with validation
     * @return ResponseEntity with the updated role and appropriate HTTP status
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        logger.info("Updating role with ID: {} and new name: {}", id, request.getName());
        
        ResponseEntity<RoleResponse> response = roleHandler.updateRole(id, request);
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            logger.info("Successfully updated role with ID: {}", response.getBody().getId());
        }
        
        return response;
    }
    
    /**
     * Deletes a role by ID.
     * 
     * DELETE /api/v1/roles/{id}
     * 
     * @param id The role ID to delete
     * @return ResponseEntity with appropriate HTTP status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        logger.info("Deleting role with ID: {}", id);
        
        ResponseEntity<Void> response = roleHandler.deleteRole(id);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Successfully deleted role with ID: {}", id);
        }
        
        return response;
    }
    
    /**
     * Retrieves a specific role by ID.
     * 
     * GET /api/v1/roles/{id}
     * 
     * @param id The unique identifier of the role to retrieve
     * @return ResponseEntity with the role and appropriate HTTP status
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        logger.info("Retrieving role with ID: {}", id);
        
        ResponseEntity<RoleResponse> response = roleHandler.getRoleById(id);
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            logger.info("Successfully retrieved role: {}", response.getBody().getName());
        }
        
        return response;
    }
    
    /**
     * Retrieves all roles.
     * 
     * GET /api/v1/roles
     * 
     * @return ResponseEntity with list of all roles and HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        logger.info("Retrieving all roles");
        
        ResponseEntity<List<RoleResponse>> response = roleHandler.getAllRoles();
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            logger.info("Successfully retrieved {} roles", response.getBody().size());
        }
        
        return response;
    }
    
    /**
     * Searches roles by name pattern.
     * 
     * GET /api/v1/roles/search?name={pattern}
     * 
     * @param name The search pattern
     * @return ResponseEntity with list of matching roles
     */
    @GetMapping("/search")
    public ResponseEntity<List<RoleResponse>> searchRoles(@RequestParam String name) {
        logger.info("Searching roles with pattern: {}", name);
        
        ResponseEntity<List<RoleResponse>> response = roleHandler.searchRoles(name);
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            logger.info("Found {} roles matching pattern: {}", response.getBody().size(), name);
        }
        
        return response;
    }
    
    /**
     * Gets the total count of roles.
     * 
     * GET /api/v1/roles/count
     * 
     * @return ResponseEntity with the role count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getRoleCount() {
        logger.info("Getting role count");
        
        ResponseEntity<Map<String, Long>> response = roleHandler.getRoleCount();
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            logger.info("Total roles count: {}", response.getBody().get("count"));
        }
        
        return response;
    }
}