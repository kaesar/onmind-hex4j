package co.onmind.microhex.domain.services;

import co.onmind.microhex.domain.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the RoleService domain service.
 * 
 * These tests verify the business logic and validation rules
 * implemented in the RoleService.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@DisplayName("RoleService Domain Service Tests")
class RoleServiceTest {
    
    private RoleService roleService;
    
    @BeforeEach
    void setUp() {
        roleService = new RoleService();
    }
    
    @Nested
    @DisplayName("Create Role Tests")
    class CreateRoleTests {
        
        @Test
        @DisplayName("Should create role with valid name")
        void shouldCreateRoleWithValidName() {
            // Given
            String roleName = "ADMIN";
            
            // When
            Role role = roleService.createRole(roleName);
            
            // Then
            assertNotNull(role);
            assertEquals(roleName, role.getName());
            assertNotNull(role.getCreatedAt());
            assertTrue(roleService.isRoleActive(role));
        }
        
        @Test
        @DisplayName("Should trim whitespace when creating role")
        void shouldTrimWhitespaceWhenCreatingRole() {
            // Given
            String roleNameWithSpaces = "  ADMIN  ";
            
            // When
            Role role = roleService.createRole(roleNameWithSpaces);
            
            // Then
            assertEquals("ADMIN", role.getName());
        }
        
        @Test
        @DisplayName("Should reject null name when creating role")
        void shouldRejectNullNameWhenCreatingRole() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.createRole(null)
            );
            assertEquals("Role name cannot be null or empty", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should reject empty name when creating role")
        void shouldRejectEmptyNameWhenCreatingRole() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.createRole("")
            );
            assertEquals("Role name cannot be null or empty", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should reject reserved names when creating role")
        void shouldRejectReservedNamesWhenCreatingRole() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> roleService.createRole("SYSTEM"));
            assertThrows(IllegalArgumentException.class, () -> roleService.createRole("ROOT"));
            assertThrows(IllegalArgumentException.class, () -> roleService.createRole("NULL"));
            assertThrows(IllegalArgumentException.class, () -> roleService.createRole("UNDEFINED"));
        }
        
        @Test
        @DisplayName("Should reject system prefixes when creating role")
        void shouldRejectSystemPrefixesWhenCreatingRole() {
            // When & Then
            IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.createRole("SYS_ADMIN")
            );
            assertTrue(exception1.getMessage().contains("system prefixes"));
            
            IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.createRole("INTERNAL_USER")
            );
            assertTrue(exception2.getMessage().contains("system prefixes"));
        }
    }
    
    @Nested
    @DisplayName("Update Role Tests")
    class UpdateRoleTests {
        
        @Test
        @DisplayName("Should update role with valid new name")
        void shouldUpdateRoleWithValidNewName() {
            // Given
            Role existingRole = new Role("OLD_NAME");
            String newName = "NEW_NAME";
            
            // When
            Role updatedRole = roleService.updateRole(existingRole, newName);
            
            // Then
            assertSame(existingRole, updatedRole);
            assertEquals(newName, updatedRole.getName());
        }
        
        @Test
        @DisplayName("Should trim whitespace when updating role")
        void shouldTrimWhitespaceWhenUpdatingRole() {
            // Given
            Role existingRole = new Role("OLD_NAME");
            String newNameWithSpaces = "  NEW_NAME  ";
            
            // When
            Role updatedRole = roleService.updateRole(existingRole, newNameWithSpaces);
            
            // Then
            assertEquals("NEW_NAME", updatedRole.getName());
        }
        
        @Test
        @DisplayName("Should reject null existing role when updating")
        void shouldRejectNullExistingRoleWhenUpdating() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.updateRole(null, "NEW_NAME")
            );
            assertEquals("Existing role cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should reject invalid new name when updating")
        void shouldRejectInvalidNewNameWhenUpdating() {
            // Given
            Role existingRole = new Role("OLD_NAME");
            
            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> roleService.updateRole(existingRole, ""));
            assertThrows(IllegalArgumentException.class, 
                () -> roleService.updateRole(existingRole, "SYSTEM"));
        }
    }
    
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Should validate role business rules for valid role")
        void shouldValidateRoleBusinessRulesForValidRole() {
            // Given
            Role validRole = new Role("VALID_ROLE");
            
            // When & Then
            assertDoesNotThrow(() -> roleService.validateRoleBusinessRules(validRole));
        }
        
        @Test
        @DisplayName("Should reject null role in business rules validation")
        void shouldRejectNullRoleInBusinessRulesValidation() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.validateRoleBusinessRules(null)
            );
            assertEquals("Role cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should reject inactive role in business rules validation")
        void shouldRejectInactiveRoleInBusinessRulesValidation() {
            // Given
            Role inactiveRole = new Role();
            inactiveRole.setName("VALID_NAME");
            // createdAt is null, making it inactive
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.validateRoleBusinessRules(inactiveRole)
            );
            assertEquals("Role must be in an active state", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should validate role deletion for valid role")
        void shouldValidateRoleDeletionForValidRole() {
            // Given
            Role validRole = new Role("VALID_ROLE");
            
            // When & Then
            assertDoesNotThrow(() -> roleService.validateRoleDeletion(validRole));
        }
        
        @Test
        @DisplayName("Should reject null role in deletion validation")
        void shouldRejectNullRoleInDeletionValidation() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.validateRoleDeletion(null)
            );
            assertEquals("Role cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should reject system role deletion")
        void shouldRejectSystemRoleDeletion() {
            // Given
            Role systemRole = new Role("SYSTEM_ADMIN");
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.validateRoleDeletion(systemRole)
            );
            assertEquals("System roles cannot be deleted", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Name Validation Tests")
    class NameValidationTests {
        
        @Test
        @DisplayName("Should return true for valid role names")
        void shouldReturnTrueForValidRoleNames() {
            // When & Then
            assertTrue(roleService.isValidRoleName("VALID_ROLE"));
            assertTrue(roleService.isValidRoleName("User-Manager"));
            assertTrue(roleService.isValidRoleName("Role123"));
        }
        
        @Test
        @DisplayName("Should return false for invalid role names")
        void shouldReturnFalseForInvalidRoleNames() {
            // When & Then
            assertFalse(roleService.isValidRoleName(null));
            assertFalse(roleService.isValidRoleName(""));
            assertFalse(roleService.isValidRoleName("A"));
            assertFalse(roleService.isValidRoleName("SYSTEM"));
            assertFalse(roleService.isValidRoleName("SYS_ADMIN"));
            assertFalse(roleService.isValidRoleName("ROLE@INVALID"));
        }
    }
    
    @Nested
    @DisplayName("Name Normalization Tests")
    class NameNormalizationTests {
        
        @Test
        @DisplayName("Should normalize role name correctly")
        void shouldNormalizeRoleNameCorrectly() {
            // When & Then
            assertEquals("ADMIN", roleService.normalizeRoleName("  ADMIN  "));
            assertEquals("USER MANAGER", roleService.normalizeRoleName("USER   MANAGER"));
            assertEquals("ROLE", roleService.normalizeRoleName("ROLE"));
        }
        
        @Test
        @DisplayName("Should handle null in normalization")
        void shouldHandleNullInNormalization() {
            // When & Then
            assertNull(roleService.normalizeRoleName(null));
        }
        
        @Test
        @DisplayName("Should replace multiple spaces with single space")
        void shouldReplaceMultipleSpacesWithSingleSpace() {
            // When & Then
            assertEquals("USER MANAGER ROLE", 
                roleService.normalizeRoleName("USER    MANAGER     ROLE"));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle role name at minimum length")
        void shouldHandleRoleNameAtMinimumLength() {
            // Given
            String minLengthName = "AB";
            
            // When & Then
            assertDoesNotThrow(() -> roleService.createRole(minLengthName));
            assertTrue(roleService.isValidRoleName(minLengthName));
        }
        
        @Test
        @DisplayName("Should handle role name at maximum length")
        void shouldHandleRoleNameAtMaximumLength() {
            // Given
            String maxLengthName = "A".repeat(50);
            
            // When & Then
            assertDoesNotThrow(() -> roleService.createRole(maxLengthName));
            assertTrue(roleService.isValidRoleName(maxLengthName));
        }
        
        @Test
        @DisplayName("Should handle case insensitive reserved names")
        void shouldHandleCaseInsensitiveReservedNames() {
            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> roleService.createRole("system"));
            assertThrows(IllegalArgumentException.class, 
                () -> roleService.createRole("System"));
            assertThrows(IllegalArgumentException.class, 
                () -> roleService.createRole("SYSTEM"));
        }
        
        @Test
        @DisplayName("Should handle case insensitive system prefixes")
        void shouldHandleCaseInsensitiveSystemPrefixes() {
            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> roleService.createRole("sys_admin"));
            assertThrows(IllegalArgumentException.class, 
                () -> roleService.createRole("Sys_Admin"));
            assertThrows(IllegalArgumentException.class, 
                () -> roleService.createRole("internal_user"));
        }
    }
}