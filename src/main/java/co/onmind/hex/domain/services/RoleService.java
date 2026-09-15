package co.onmind.hex.domain.services;

import co.onmind.hex.domain.models.Role;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleService {

    public Role createRole(String name) {
        validateRoleName(name);

        Role role = new Role();
        role.setName(name.trim());
        role.setCreatedAt(LocalDateTime.now());

        return validateRoleBusinessRules(role);
    }

    public Role updateRole(Role existingRole, String newName) {
        if (existingRole == null) {
            throw new IllegalArgumentException("Existing role cannot be null");
        }

        validateRoleName(newName);
        existingRole.setName(newName.trim());

        return validateRoleBusinessRules(existingRole);
    }

    public Role validateRoleBusinessRules(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        validateRoleName(role.getName());
        validateRoleNameBusinessRules(role.getName());

        if (!isRoleActive(role)) {
            throw new IllegalArgumentException("Role must be in an active state");
        }

        return role;
    }

    public void validateRoleDeletion(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        if (role.getName() != null && role.getName().toUpperCase().contains("SYSTEM")) {
            throw new IllegalArgumentException("System roles cannot be deleted");
        }
    }

    public boolean isValidRoleName(String name) {
        try {
            validateRoleName(name);
            validateRoleNameBusinessRules(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String normalizeRoleName(String name) {
        if (name == null) {
            return null;
        }

        String normalized = name.trim();
        normalized = normalized.replaceAll("\\s+", " ");

        return normalized;
    }

    public boolean isRoleActive(Role role) {
        return role != null
            && role.getName() != null
            && !role.getName().trim().isEmpty()
            && role.getCreatedAt() != null;
    }

    public List<Role> findActiveRoles(List<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
            .filter(this::isRoleActive)
            .collect(Collectors.toList());
    }

    private void validateRoleName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }

        String trimmedName = name.trim();

        if (trimmedName.length() < 2) {
            throw new IllegalArgumentException("Role name must be at least 2 characters long");
        }

        if (trimmedName.length() > 50) {
            throw new IllegalArgumentException("Role name cannot exceed 50 characters");
        }

        if (!trimmedName.matches("^[a-zA-Z0-9_\\s-]+$")) {
            throw new IllegalArgumentException("Role name can only contain letters, numbers, spaces, hyphens and underscores");
        }
    }

    private void validateRoleNameBusinessRules(String name) {
        List<String> reservedNames = List.of("SYSTEM", "ROOT", "NULL", "UNDEFINED");

        if (reservedNames.contains(name.toUpperCase())) {
            throw new IllegalArgumentException("Role name '" + name + "' is reserved and cannot be used");
        }

        if (name.toUpperCase().startsWith("SYS_") || name.toUpperCase().startsWith("INTERNAL_")) {
            throw new IllegalArgumentException("Role name cannot start with system prefixes (SYS_, INTERNAL_)");
        }
    }
}
