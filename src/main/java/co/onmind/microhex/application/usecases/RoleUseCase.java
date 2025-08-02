package co.onmind.microhex.application.usecases;

import co.onmind.microhex.application.dto.in.CreateRoleRequestDto;
import co.onmind.microhex.application.dto.out.RoleResponseDto;
import co.onmind.microhex.application.mappers.RoleMapper;
import co.onmind.microhex.application.ports.in.CreateRoleTrait;
import co.onmind.microhex.application.ports.in.GetRoleTrait;
import co.onmind.microhex.application.ports.out.RoleRepositoryPort;
import co.onmind.microhex.domain.exceptions.DuplicateRoleException;
import co.onmind.microhex.domain.exceptions.RoleNotFoundException;
import co.onmind.microhex.domain.models.Role;
import co.onmind.microhex.domain.services.RoleService;
import co.onmind.microhex.infrastructure.webclients.NotificationWebClient;
import co.onmind.microhex.infrastructure.webclients.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of role use cases.
 * 
 * This class orchestrates the role-related operations by coordinating between
 * domain services, repository ports, and mappers. It implements both create
 * and query use cases for roles following the hexagonal architecture pattern.
 * 
 * The implementation handles:
 * - Input validation and transformation
 * - Business rule enforcement through domain services
 * - Data persistence coordination through repository ports
 * - Response transformation through mappers
 * - Exception handling and propagation
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@Service
@Slf4j
public class RoleUseCase implements CreateRoleTrait, GetRoleTrait {
    
    private final RoleService roleService;
    private final RoleRepositoryPort roleRepositoryPort;
    private final RoleMapper roleMapper;
    private final NotificationWebClient notificationWebClient;

    /**
     * Constructor for dependency injection.
     * 
     * @param roleService Domain service for role business logic
     * @param roleRepositoryPort Output port for role persistence
     * @param roleMapper Mapper for DTO transformations
     * @param notificationWebClient Web client for notifications
     */
    public RoleUseCase(
            RoleService roleService,
            RoleRepositoryPort roleRepositoryPort,
            RoleMapper roleMapper,
            NotificationWebClient notificationWebClient) {
        this.roleService = roleService;
        this.roleRepositoryPort = roleRepositoryPort;
        this.roleMapper = roleMapper;
        this.notificationWebClient = notificationWebClient;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RoleResponseDto createRole(CreateRoleRequestDto request) {
        // Input validation
        if (request == null) {
            throw new IllegalArgumentException("Create role request cannot be null");
        }
        
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }
        
        String roleName = request.name().trim();
        
        // Check for duplicate role names
        if (roleRepositoryPort.existsByName(roleName)) {
            throw DuplicateRoleException.forName(roleName);
        }
        
        // Create role using domain service (includes business validation)
        Role role = roleService.createRole(roleName);
        
        // Persist the role
        Role savedRole = roleRepositoryPort.save(role);
        
        // Send notification
        try {
            NotificationRequest notification = new NotificationRequest("Role created: " + savedRole.getName());
            notificationWebClient.post("/api/notify", notification);
        } catch (Exception e) {
            log.error("Error sending notification for role creation: {}", savedRole.getName(), e);
        }
        
        // Transform to response DTO
        return roleMapper.toResponseDto(savedRole);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RoleResponseDto getRoleById(Long id) {
        // Input validation
        if (id == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
        
        if (id <= 0) {
            throw new IllegalArgumentException("Role ID must be positive");
        }
        
        // Retrieve role from repository
        Role role = roleRepositoryPort.findById(id)
                .orElseThrow(() -> RoleNotFoundException.forId(id));
        
        // Transform to response DTO
        return roleMapper.toResponseDto(role);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<RoleResponseDto> getAllRoles() {
        // Retrieve all roles from repository
        List<Role> roles = roleRepositoryPort.findAll();
        
        // Transform to response DTOs
        return roles.stream()
                .map(roleMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByName(String name) {
        // Input validation
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }
        
        String trimmedName = name.trim();
        
        // Validate name format using domain service
        if (!roleService.isValidRoleName(trimmedName)) {
            return false;
        }
        
        // Check existence in repository
        return roleRepositoryPort.existsByName(trimmedName);
    }
}