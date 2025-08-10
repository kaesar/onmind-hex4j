package co.onmind.microhex.domain.services;

import co.onmind.microhex.domain.exceptions.RoleAlreadyExistsException;
import co.onmind.microhex.domain.exceptions.RoleNotFoundException;
import co.onmind.microhex.domain.exceptions.SystemRoleException;
import co.onmind.microhex.domain.models.Role;
import co.onmind.microhex.domain.ports.in.RoleServicePort;
import co.onmind.microhex.domain.ports.out.NotificationPort;
import co.onmind.microhex.domain.ports.out.RoleRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Domain service that orchestrates role operations through ports.
 * 
 * This service contains the business logic for role management,
 * including both commands (write operations) and queries (read operations).
 * It implements the input port and uses output ports to interact with
 * external systems while maintaining domain independence.
 */
@Service
public class RoleService implements RoleServicePort {

    private final RoleRepositoryPort roleRepositoryPort;
    private final NotificationPort notificationPort;

    public RoleService(RoleRepositoryPort roleRepositoryPort, NotificationPort notificationPort) {
        this.roleRepositoryPort = roleRepositoryPort;
        this.notificationPort = notificationPort;
    }

    // ========== COMMANDS (Write Operations) ==========

    /**
     * Creates a new role in the system.
     */
    @Override
    public Role createRole(String name) {
        String normalizedName = name.trim().toUpperCase();

        // Business rule: Check if role already exists
        if (roleRepositoryPort.existsByName(normalizedName)) {
            throw new RoleAlreadyExistsException("Role with name '" + normalizedName + "' already exists");
        }

        Role role = Role.create(normalizedName);
        Role savedRole = roleRepositoryPort.save(role);

        // Notify external systems asynchronously using Virtual Threads (Java 21)
        Thread.startVirtualThread(() -> {
            try {
                notificationPort.notifyRoleCreated(savedRole);
            } catch (Exception e) {
                // Log error but don't fail the main operation
                System.err.println("Failed to send notification: " + e.getMessage());
            }
        });

        return savedRole;
    }

    /**
     * Updates an existing role's name.
     */
    @Override
    public Role updateRole(Long id, String newName) {
        Role existingRole = roleRepositoryPort.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role with ID " + id + " not found"));

        // Business rule: Cannot update system roles
        if (existingRole.isSystemRole()) {
            throw new SystemRoleException("Cannot update system role: " + existingRole.getName());
        }

        String normalizedName = newName.trim().toUpperCase();

        // Business rule: Check if new name already exists (excluding current role)
        roleRepositoryPort.findByName(normalizedName).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RoleAlreadyExistsException("Role with name '" + normalizedName + "' already exists");
            }
        });

        Role updatedRole = existingRole.withName(normalizedName);
        Role savedRole = roleRepositoryPort.save(updatedRole);

        // Notify external systems asynchronously using Virtual Threads (Java 21)
        Thread.startVirtualThread(() -> {
            try {
                notificationPort.notifyRoleUpdated(savedRole);
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }
        });

        return savedRole;
    }

    /**
     * Deletes a role from the system.
     */
    @Override
    public void deleteRole(Long id) {
        Role role = roleRepositoryPort.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role with ID " + id + " not found"));

        // Business rule: Cannot delete system roles
        if (role.isSystemRole()) {
            throw new SystemRoleException("Cannot delete system role: " + role.getName());
        }

        boolean deleted = roleRepositoryPort.deleteById(id);
        if (!deleted) {
            throw new RoleNotFoundException("Role with ID " + id + " could not be deleted");
        }

        // Notify external systems asynchronously using Virtual Threads (Java 21)
        Thread.startVirtualThread(() -> {
            try {
                notificationPort.notifyRoleDeleted(id);
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }
        });
    }

    // ========== QUERIES (Read Operations) ==========

    /**
     * Retrieves a role by its ID.
     */
    @Override
    public Optional<Role> getRoleById(Long id) {
        return roleRepositoryPort.findById(id);
    }

    /**
     * Retrieves a role by its name.
     */
    @Override
    public Optional<Role> getRoleByName(String name) {
        return roleRepositoryPort.findByName(name.trim().toUpperCase());
    }

    /**
     * Retrieves all roles in the system.
     */
    @Override
    public List<Role> getAllRoles() {
        return roleRepositoryPort.findAll();
    }

    /**
     * Searches roles by name pattern.
     */
    @Override
    public List<Role> searchRolesByName(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Search pattern cannot be blank");
        }
        return roleRepositoryPort.findByNameContaining(pattern.trim());
    }

    /**
     * Gets the total count of roles.
     */
    @Override
    public Long getRoleCount() {
        return roleRepositoryPort.count();
    }

    /**
     * Checks if a role exists by name.
     */
    @Override
    public boolean roleExists(String name) {
        return roleRepositoryPort.existsByName(name.trim().toUpperCase());
    }
}