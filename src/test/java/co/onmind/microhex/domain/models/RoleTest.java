package co.onmind.microhex.domain.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Role domain model.
 * 
 * These tests verify the business logic and validation rules
 * implemented in the Role domain model.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 2.0.0
 */
@DisplayName("Role Domain Model Tests")
class RoleTest {
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create role with name constructor")
        void shouldCreateRoleWithNameConstructor() {
            // Given
            String roleName = "admin";
            
            // When
            Role role = new Role(roleName);
            
            // Then
            assertNotNull(role);
            assertEquals("ADMIN", role.getName()); // Should be normalized to uppercase
            assertNotNull(role.getCreatedAt());
            assertNull(role.getId());
        }
        
        @Test
        @DisplayName("Should create role with full constructor")
        void shouldCreateRoleWithFullConstructor() {
            // Given
            Long id = 1L;
            String name = "user";
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            
            // When
            Role role = new Role(id, name, createdAt);
            
            // Then
            assertNotNull(role);
            assertEquals(id, role.getId());
            assertEquals("USER", role.getName()); // Should be normalized to uppercase
            assertEquals(createdAt, role.getCreatedAt());
        }
        
        @Test
        @DisplayName("Should create role with default constructor")
        void shouldCreateRoleWithDefaultConstructor() {
            // When
            Role role = new Role();
            
            // Then
            assertNotNull(role);
            assertNull(role.getId());
            assertNull(role.getName());
            assertNull(role.getCreatedAt());
        }
    }
    
    @Nested
    @DisplayName("Name Validation Tests")
    class NameValidationTests {
        
        @Test
        @DisplayName("Should create role with valid names")
        void shouldCreateRoleWithValidNames() {
            // Given & When & Then
            assertDoesNotThrow(() -> new Role("ADMIN"));
            assertDoesNotThrow(() -> new Role("USER_MANAGER"));
            assertDoesNotThrow(() -> new Role("Data-Analyst"));
            assertDoesNotThrow(() -> new Role("Role123"));
            assertDoesNotThrow(() -> new Role("My Role"));
        }
        
        @Test
        @DisplayName("Should throw exception for null name")
        void shouldThrowExceptionForNullName() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> new Role(null)
            );
            assertEquals("Role name cannot be blank", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for empty name")
        void shouldThrowExceptionForEmptyName() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> new Role("")
            );
            assertEquals("Role name cannot be blank", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for blank name")
        void shouldThrowExceptionForBlankName() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> new Role("   ")
            );
            assertEquals("Role name cannot be blank", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for name too long")
        void shouldThrowExceptionForNameTooLong() {
            // Given
            String longName = "A".repeat(101);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> new Role(longName)
            );
            assertEquals("Role name cannot exceed 100 characters", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should identify system roles correctly")
        void shouldIdentifySystemRolesCorrectly() {
            // Given & When & Then
            assertTrue(new Role("ADMIN").isSystemRole());
            assertTrue(new Role("ROOT").isSystemRole());
            assertTrue(new Role("SYSTEM").isSystemRole());
            assertTrue(new Role("SYSTEM_ADMIN").isSystemRole());
            assertFalse(new Role("USER").isSystemRole());
            assertFalse(new Role("MANAGER").isSystemRole());
        }
        
        @Test
        @DisplayName("Should create role with factory method")
        void shouldCreateRoleWithFactoryMethod() {
            // Given
            String roleName = "developer";
            
            // When
            Role role = Role.create(roleName);
            
            // Then
            assertNotNull(role);
            assertEquals("DEVELOPER", role.getName());
            assertNotNull(role.getCreatedAt());
            assertNull(role.getId());
        }
        
        @Test
        @DisplayName("Should create new role with updated name")
        void shouldCreateNewRoleWithUpdatedName() {
            // Given
            Role originalRole = new Role(1L, "OLD_NAME", LocalDateTime.now());
            String newName = "new_name";
            
            // When
            Role updatedRole = originalRole.withName(newName);
            
            // Then
            assertNotNull(updatedRole);
            assertEquals(originalRole.getId(), updatedRole.getId());
            assertEquals("NEW_NAME", updatedRole.getName());
            assertEquals(originalRole.getCreatedAt(), updatedRole.getCreatedAt());
            // Original role should remain unchanged
            assertEquals("OLD_NAME", originalRole.getName());
        }
        
        @Test
        @DisplayName("Should normalize name to uppercase")
        void shouldNormalizeNameToUppercase() {
            // Given & When
            Role role1 = new Role("admin");
            Role role2 = new Role("  User  ");
            Role role3 = Role.create("manager");
            
            // Then
            assertEquals("ADMIN", role1.getName());
            assertEquals("USER", role2.getName());
            assertEquals("MANAGER", role3.getName());
        }
    }
    
    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityAndHashTests {
        
        @Test
        @DisplayName("Should be equal when id and name are same")
        void shouldBeEqualWhenIdAndNameAreSame() {
            // Given
            Role role1 = new Role(1L, "ADMIN", LocalDateTime.now());
            Role role2 = new Role(1L, "ADMIN", LocalDateTime.now().plusHours(1));
            
            // When & Then
            assertEquals(role1, role2);
            assertEquals(role1.hashCode(), role2.hashCode());
        }
        
        @Test
        @DisplayName("Should not be equal when names are different")
        void shouldNotBeEqualWhenNamesAreDifferent() {
            // Given
            Role role1 = new Role(1L, "ADMIN", LocalDateTime.now());
            Role role2 = new Role(1L, "USER", LocalDateTime.now());
            
            // When & Then
            assertNotEquals(role1, role2);
        }
        
        @Test
        @DisplayName("Should not be equal when ids are different")
        void shouldNotBeEqualWhenIdsAreDifferent() {
            // Given
            Role role1 = new Role(1L, "ADMIN", LocalDateTime.now());
            Role role2 = new Role(2L, "ADMIN", LocalDateTime.now());
            
            // When & Then
            assertNotEquals(role1, role2);
        }
        
        @Test
        @DisplayName("Should handle null in equals")
        void shouldHandleNullInEquals() {
            // Given
            Role role = new Role("ADMIN");
            
            // When & Then
            assertNotEquals(role, null);
            assertEquals(role, role);
        }
    }
    
    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("Should generate meaningful toString")
        void shouldGenerateMeaningfulToString() {
            // Given
            Role role = new Role(1L, "ADMIN", LocalDateTime.of(2023, 1, 1, 12, 0));
            
            // When
            String toString = role.toString();
            
            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("Role{"));
            assertTrue(toString.contains("id=1"));
            assertTrue(toString.contains("name='ADMIN'"));
            assertTrue(toString.contains("createdAt="));
        }
    }
    
    @Nested
    @DisplayName("Setter Tests")
    class SetterTests {
        
        @Test
        @DisplayName("Should set and get id correctly")
        void shouldSetAndGetIdCorrectly() {
            // Given
            Role role = new Role();
            Long id = 123L;
            
            // When
            role.setId(id);
            
            // Then
            assertEquals(id, role.getId());
        }
        
        @Test
        @DisplayName("Should set and get name correctly")
        void shouldSetAndGetNameCorrectly() {
            // Given
            Role role = new Role();
            String name = "TEST_ROLE";
            
            // When
            role.setName(name);
            
            // Then
            assertEquals(name, role.getName());
        }
        
        @Test
        @DisplayName("Should set and get createdAt correctly")
        void shouldSetAndGetCreatedAtCorrectly() {
            // Given
            Role role = new Role();
            LocalDateTime createdAt = LocalDateTime.now();
            
            // When
            role.setCreatedAt(createdAt);
            
            // Then
            assertEquals(createdAt, role.getCreatedAt());
        }
    }
}