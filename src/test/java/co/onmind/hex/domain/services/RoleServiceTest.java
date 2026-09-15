package co.onmind.hex.domain.services;

import co.onmind.hex.domain.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleServiceTest {

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleService();
    }

    @Test
    @DisplayName("createRole creates a valid role")
    void createRoleValid() {
        Role role = roleService.createRole("ADMIN");
        assertNotNull(role);
        assertEquals("ADMIN", role.getName());
        assertNotNull(role.getCreatedAt());
    }

    @Test
    @DisplayName("createRole rejects blank names")
    void createRoleBlank() {
        assertThrows(IllegalArgumentException.class, () -> roleService.createRole(null));
        assertThrows(IllegalArgumentException.class, () -> roleService.createRole("  "));
    }

    @Test
    @DisplayName("createRole rejects too short and too long names")
    void createRoleLength() {
        assertThrows(IllegalArgumentException.class, () -> roleService.createRole("A"));
        assertThrows(IllegalArgumentException.class, () -> roleService.createRole("A".repeat(51)));
    }

    @Test
    @DisplayName("createRole rejects invalid characters")
    void createRoleInvalidChars() {
        assertThrows(IllegalArgumentException.class, () -> roleService.createRole("ADMIN!"));
    }

    @Test
    @DisplayName("createRole rejects reserved names")
    void createRoleReserved() {
        assertThrows(IllegalArgumentException.class, () -> roleService.createRole("SYSTEM"));
        assertThrows(IllegalArgumentException.class, () -> roleService.createRole("root"));
        assertThrows(IllegalArgumentException.class, () -> roleService.createRole("SYS_ADMIN"));
        assertThrows(IllegalArgumentException.class, () -> roleService.createRole("INTERNAL_X"));
    }

    @Test
    @DisplayName("updateRole updates name")
    void updateRoleOk() {
        Role existing = new Role(1L, "USER", LocalDateTime.now());
        Role updated = roleService.updateRole(existing, "EDITOR");
        assertEquals("EDITOR", updated.getName());
    }

    @Test
    @DisplayName("updateRole rejects null role")
    void updateRoleNull() {
        assertThrows(IllegalArgumentException.class, () -> roleService.updateRole(null, "EDITOR"));
    }

    @Test
    @DisplayName("validateRoleDeletion rejects system roles")
    void validateDeletionSystem() {
        Role system = new Role(1L, "SYSTEM_ADMIN", LocalDateTime.now());
        assertThrows(IllegalArgumentException.class, () -> roleService.validateRoleDeletion(system));
    }

    @Test
    @DisplayName("validateRoleDeletion accepts normal roles")
    void validateDeletionNormal() {
        Role role = new Role(1L, "USER", LocalDateTime.now());
        assertDoesNotThrow(() -> roleService.validateRoleDeletion(role));
    }

    @Test
    @DisplayName("isValidRoleName reports validity")
    void isValidRoleName() {
        assertTrue(roleService.isValidRoleName("EDITOR"));
        assertFalse(roleService.isValidRoleName(""));
        assertFalse(roleService.isValidRoleName("SYSTEM"));
    }

    @Test
    @DisplayName("normalizeRoleName collapses whitespace")
    void normalizeRoleName() {
        assertEquals("A B", roleService.normalizeRoleName("  A   B  "));
        assertNull(roleService.normalizeRoleName(null));
    }

    @Test
    @DisplayName("findActiveRoles filters inactive roles")
    void findActiveRoles() {
        Role active = new Role(1L, "USER", LocalDateTime.now());
        Role inactive = new Role();
        inactive.setName("GHOST");
        List<Role> result = roleService.findActiveRoles(List.of(active, inactive));
        assertEquals(1, result.size());
        assertEquals("USER", result.get(0).getName());
    }
}
