package co.onmind.microhex.application.handlers;

import co.onmind.microhex.application.dto.CreateRoleRequest;
import co.onmind.microhex.application.dto.RoleResponse;
import co.onmind.microhex.application.dto.UpdateRoleRequest;
import co.onmind.microhex.application.mappers.RoleMapper;
import co.onmind.microhex.domain.exceptions.RoleAlreadyExistsException;
import co.onmind.microhex.domain.exceptions.RoleNotFoundException;
import co.onmind.microhex.domain.exceptions.SystemRoleException;
import co.onmind.microhex.domain.models.Role;
import co.onmind.microhex.domain.ports.in.RoleServicePort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Application handler for role operations.
 * 
 * This handler acts as an orchestrator between the REST controllers
 * and the domain service, handling DTOs conversion and HTTP responses.
 * It follows the hexagonal architecture variation where the application
 * layer is simplified with handlers instead of traditional use cases.
 */
@Component
public class RoleHandler {
    
    private final RoleServicePort roleServicePort;
    private final RoleMapper roleMapper;
    
    public RoleHandler(RoleServicePort roleServicePort, RoleMapper roleMapper) {
        this.roleServicePort = roleServicePort;
        this.roleMapper = roleMapper;
    }
    
    /**
     * Handles role creation requests.
     */
    public ResponseEntity<RoleResponse> createRole(CreateRoleRequest request) {
        try {
            Role role = roleServicePort.createRole(request.getName());
            RoleResponse response = roleMapper.toResponse(role);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RoleAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Handles role update requests.
     */
    public ResponseEntity<RoleResponse> updateRole(Long id, UpdateRoleRequest request) {
        try {
            Role role = roleServicePort.updateRole(id, request.getName());
            RoleResponse response = roleMapper.toResponse(role);
            return ResponseEntity.ok(response);
        } catch (RoleNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RoleAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (SystemRoleException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Handles role deletion requests.
     */
    public ResponseEntity<Void> deleteRole(Long id) {
        try {
            roleServicePort.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (RoleNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SystemRoleException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Handles get role by ID requests.
     */
    public ResponseEntity<RoleResponse> getRoleById(Long id) {
        Optional<Role> role = roleServicePort.getRoleById(id);
        if (role.isPresent()) {
            RoleResponse response = roleMapper.toResponse(role.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Handles get all roles requests.
     */
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<Role> roles = roleServicePort.getAllRoles();
        List<RoleResponse> responses = roleMapper.toResponseList(roles);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Handles search roles requests.
     */
    public ResponseEntity<List<RoleResponse>> searchRoles(String name) {
        try {
            List<Role> roles = roleServicePort.searchRolesByName(name);
            List<RoleResponse> responses = roleMapper.toResponseList(roles);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Handles get role count requests.
     */
    public ResponseEntity<Map<String, Long>> getRoleCount() {
        Long count = roleServicePort.getRoleCount();
        return ResponseEntity.ok(Map.of("count", count));
    }
}