package co.onmind.microhex.infrastructure.persistence.mappers;

import co.onmind.microhex.domain.models.Role;
import co.onmind.microhex.infrastructure.persistence.entities.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the RoleEntityMapper using MapStruct.
 * 
 * These tests verify the mapping functionality between domain models
 * and JPA entities using the MapStruct generated implementation.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@SpringBootTest(classes = {RoleEntityMapperImpl.class})
@ActiveProfiles("test")
@DisplayName("Role Entity Mapper Tests")
class RoleEntityMapperTest {
    
    @Autowired
    private RoleEntityMapper roleEntityMapper;
    
    @Nested
    @DisplayName("Entity to Domain Mapping Tests")
    class EntityToDomainMappingTests {
        
        @Test
        @DisplayName("Should map RoleEntity to Role correctly")
        void shouldMapRoleEntityToRoleCorrectly() {
            // Given
            Long expectedId = 1L;
            String expectedName = "ADMIN";
            LocalDateTime expectedCreatedAt = LocalDateTime.of(2023, 1, 1, 12, 0);
            RoleEntity entity = new RoleEntity(expectedId, expectedName, expectedCreatedAt);
            
            // When
            Role role = roleEntityMapper.toDomain(entity);
            
            // Then
            assertNotNull(role);
            assertEquals(expectedId, role.getId());
            assertEquals(expectedName, role.getName());
            assertEquals(expectedCreatedAt, role.getCreatedAt());
        }
        
        @Test
        @DisplayName("Should map list of RoleEntities to list of Roles correctly")
        void shouldMapListOfRoleEntitiesToListOfRolesCorrectly() {
            // Given
            RoleEntity entity1 = new RoleEntity(1L, "ADMIN", LocalDateTime.now());
            RoleEntity entity2 = new RoleEntity(2L, "USER", LocalDateTime.now());
            List<RoleEntity> entities = Arrays.asList(entity1, entity2);
            
            // When
            List<Role> roles = roleEntityMapper.toDomainList(entities);
            
            // Then
            assertNotNull(roles);
            assertEquals(2, roles.size());
            assertEquals("ADMIN", roles.get(0).getName());
            assertEquals("USER", roles.get(1).getName());
        }
    }
    
    @Nested
    @DisplayName("Domain to Entity Mapping Tests")
    class DomainToEntityMappingTests {
        
        @Test
        @DisplayName("Should map Role to RoleEntity correctly")
        void shouldMapRoleToRoleEntityCorrectly() {
            // Given
            Long expectedId = 1L;
            String expectedName = "ADMIN";
            LocalDateTime expectedCreatedAt = LocalDateTime.of(2023, 1, 1, 12, 0);
            Role role = new Role(expectedId, expectedName, expectedCreatedAt);
            
            // When
            RoleEntity entity = roleEntityMapper.toEntity(role);
            
            // Then
            assertNotNull(entity);
            assertEquals(expectedId, entity.getId());
            assertEquals(expectedName, entity.getName());
            assertEquals(expectedCreatedAt, entity.getCreatedAt());
        }
        
        @Test
        @DisplayName("Should map Role to new RoleEntity without ID")
        void shouldMapRoleToNewRoleEntityWithoutId() {
            // Given
            Long originalId = 1L;
            String expectedName = "ADMIN";
            LocalDateTime expectedCreatedAt = LocalDateTime.of(2023, 1, 1, 12, 0);
            Role role = new Role(originalId, expectedName, expectedCreatedAt);
            
            // When
            RoleEntity entity = roleEntityMapper.toNewEntity(role);
            
            // Then
            assertNotNull(entity);
            assertNull(entity.getId()); // Should be ignored for new entities
            assertEquals(expectedName, entity.getName());
            assertEquals(expectedCreatedAt, entity.getCreatedAt());
        }
    }
    
    @Nested
    @DisplayName("Null Handling Tests")
    class NullHandlingTests {
        
        @Test
        @DisplayName("Should handle null RoleEntity when mapping to domain")
        void shouldHandleNullRoleEntityWhenMappingToDomain() {
            // Given
            RoleEntity entity = null;
            
            // When
            Role role = roleEntityMapper.toDomain(entity);
            
            // Then
            assertNull(role);
        }
        
        @Test
        @DisplayName("Should handle null Role when mapping to entity")
        void shouldHandleNullRoleWhenMappingToEntity() {
            // Given
            Role role = null;
            
            // When
            RoleEntity entity = roleEntityMapper.toEntity(role);
            
            // Then
            assertNull(entity);
        }
        
        @Test
        @DisplayName("Should handle null Role when mapping to new entity")
        void shouldHandleNullRoleWhenMappingToNewEntity() {
            // Given
            Role role = null;
            
            // When
            RoleEntity entity = roleEntityMapper.toNewEntity(role);
            
            // Then
            assertNull(entity);
        }
        
        @Test
        @DisplayName("Should handle null list when mapping to domain list")
        void shouldHandleNullListWhenMappingToDomainList() {
            // Given
            List<RoleEntity> entities = null;
            
            // When
            List<Role> roles = roleEntityMapper.toDomainList(entities);
            
            // Then
            assertNull(roles);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle RoleEntity with null fields when mapping to domain")
        void shouldHandleRoleEntityWithNullFieldsWhenMappingToDomain() {
            // Given
            RoleEntity entity = new RoleEntity();
            
            // When
            Role role = roleEntityMapper.toDomain(entity);
            
            // Then
            assertNotNull(role);
            assertNull(role.getId());
            assertNull(role.getName());
            assertNull(role.getCreatedAt());
        }
        
        @Test
        @DisplayName("Should handle Role with null fields when mapping to entity")
        void shouldHandleRoleWithNullFieldsWhenMappingToEntity() {
            // Given
            Role role = new Role();
            
            // When
            RoleEntity entity = roleEntityMapper.toEntity(role);
            
            // Then
            assertNotNull(entity);
            assertNull(entity.getId());
            assertNull(entity.getName());
            assertNull(entity.getCreatedAt());
        }
        
        @Test
        @DisplayName("Should handle empty list when mapping to domain list")
        void shouldHandleEmptyListWhenMappingToDomainList() {
            // Given
            List<RoleEntity> entities = Arrays.asList();
            
            // When
            List<Role> roles = roleEntityMapper.toDomainList(entities);
            
            // Then
            assertNotNull(roles);
            assertTrue(roles.isEmpty());
        }
    }
}