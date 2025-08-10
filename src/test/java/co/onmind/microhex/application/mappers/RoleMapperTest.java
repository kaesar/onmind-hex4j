package co.onmind.microhex.application.mappers;

import co.onmind.microhex.application.dto.CreateRoleRequest;
import co.onmind.microhex.application.dto.RoleResponse;
import co.onmind.microhex.domain.models.Role;
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
 * Unit tests for the RoleMapper using MapStruct.
 * 
 * These tests verify the mapping functionality between domain models
 * and DTOs using the MapStruct generated implementation.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 2.0.0
 */
@SpringBootTest(classes = {RoleMapperImpl.class})
@ActiveProfiles("test")
@DisplayName("Role Mapper Tests")
class RoleMapperTest {
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Nested
    @DisplayName("Domain to DTO Mapping Tests")
    class DomainToDtoMappingTests {
        
        @Test
        @DisplayName("Should map Role to RoleResponse correctly")
        void shouldMapRoleToRoleResponseCorrectly() {
            // Given
            Long expectedId = 1L;
            String expectedName = "ADMIN";
            LocalDateTime expectedCreatedAt = LocalDateTime.of(2023, 1, 1, 12, 0);
            Role role = new Role(expectedId, expectedName, expectedCreatedAt);
            
            // When
            RoleResponse response = roleMapper.toResponse(role);
            
            // Then
            assertNotNull(response);
            assertEquals(expectedId, response.getId());
            assertEquals(expectedName, response.getName());
            assertEquals(expectedCreatedAt, response.getCreatedAt());
        }
        
        @Test
        @DisplayName("Should map list of Roles to list of RoleResponses correctly")
        void shouldMapListOfRolesToListOfRoleResponsesCorrectly() {
            // Given
            Role role1 = new Role(1L, "ADMIN", LocalDateTime.now());
            Role role2 = new Role(2L, "USER", LocalDateTime.now());
            List<Role> roles = Arrays.asList(role1, role2);
            
            // When
            List<RoleResponse> responses = roleMapper.toResponseList(roles);
            
            // Then
            assertNotNull(responses);
            assertEquals(2, responses.size());
            assertEquals("ADMIN", responses.get(0).getName());
            assertEquals("USER", responses.get(1).getName());
        }
    }
    
    @Nested
    @DisplayName("DTO to Domain Mapping Tests")
    class DtoToDomainMappingTests {
        
        @Test
        @DisplayName("Should map CreateRoleRequest to Role correctly")
        void shouldMapCreateRoleRequestToRoleCorrectly() {
            // Given
            String expectedName = "USER";
            CreateRoleRequest request = new CreateRoleRequest(expectedName);
            
            // When
            Role role = roleMapper.toDomain(request);
            
            // Then
            assertNotNull(role);
            assertEquals(expectedName, role.getName());
            assertNull(role.getId()); // Should be ignored in mapping
            assertNull(role.getCreatedAt()); // Should be ignored in mapping
        }
        
        @Test
        @DisplayName("Should handle CreateRoleRequest with trimmed name")
        void shouldHandleCreateRoleRequestWithTrimmedName() {
            // Given
            String inputName = "  ADMIN  ";
            CreateRoleRequest request = new CreateRoleRequest(inputName);
            
            // When
            Role role = roleMapper.toDomain(request);
            
            // Then
            assertNotNull(role);
            assertEquals(inputName, role.getName()); // MapStruct doesn't trim automatically
            assertNull(role.getId());
            assertNull(role.getCreatedAt());
        }
    }
    
    @Nested
    @DisplayName("Null Handling Tests")
    class NullHandlingTests {
        
        @Test
        @DisplayName("Should handle null Role when mapping to DTO")
        void shouldHandleNullRoleWhenMappingToDto() {
            // Given
            Role role = null;
            
            // When
            RoleResponse response = roleMapper.toResponse(role);
            
            // Then
            assertNull(response);
        }
        
        @Test
        @DisplayName("Should handle null list when mapping to response list")
        void shouldHandleNullListWhenMappingToResponseList() {
            // Given
            List<Role> roles = null;
            
            // When
            List<RoleResponse> responses = roleMapper.toResponseList(roles);
            
            // Then
            assertNull(responses);
        }
        
        @Test
        @DisplayName("Should handle null CreateRoleRequest when mapping to domain")
        void shouldHandleNullCreateRoleRequestWhenMappingToDomain() {
            // Given
            CreateRoleRequest request = null;
            
            // When
            Role role = roleMapper.toDomain(request);
            
            // Then
            assertNull(role);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle Role with null fields when mapping to DTO")
        void shouldHandleRoleWithNullFieldsWhenMappingToDto() {
            // Given
            Role role = new Role();
            
            // When
            RoleResponse response = roleMapper.toResponse(role);
            
            // Then
            assertNotNull(response);
            assertNull(response.getId());
            assertNull(response.getName());
            assertNull(response.getCreatedAt());
        }
        
        @Test
        @DisplayName("Should handle empty list when mapping to response list")
        void shouldHandleEmptyListWhenMappingToResponseList() {
            // Given
            List<Role> roles = Arrays.asList();
            
            // When
            List<RoleResponse> responses = roleMapper.toResponseList(roles);
            
            // Then
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle CreateRoleRequest with empty name")
        void shouldHandleCreateRoleRequestWithEmptyName() {
            // Given
            String emptyName = "";
            CreateRoleRequest request = new CreateRoleRequest(emptyName);
            
            // When
            Role role = roleMapper.toDomain(request);
            
            // Then
            assertNotNull(role);
            assertEquals(emptyName, role.getName());
            assertNull(role.getId());
            assertNull(role.getCreatedAt());
        }
    }
}