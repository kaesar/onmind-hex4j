package co.onmind.microhex.infrastructure.controllers;

import co.onmind.microhex.application.dto.CreateRoleRequest;
import co.onmind.microhex.application.dto.RoleResponse;
import co.onmind.microhex.application.dto.UpdateRoleRequest;
import co.onmind.microhex.application.handlers.RoleHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RoleController.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Role Controller Tests")
class RoleControllerTest {
    
    @Mock
    private RoleHandler roleHandler;
    
    @InjectMocks
    private RoleController roleController;
    
    @Nested
    @DisplayName("Create Role Tests")
    class CreateRoleTests {
        
        @Test
        @DisplayName("Should create role successfully")
        void shouldCreateRoleSuccessfully() {
            // Given
            CreateRoleRequest request = new CreateRoleRequest("ADMIN");
            RoleResponse response = new RoleResponse(1L, "ADMIN", LocalDateTime.now());
            
            when(roleHandler.createRole(any(CreateRoleRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(response));
            
            // When
            ResponseEntity<RoleResponse> result = roleController.createRole(request);
            
            // Then
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(1L, result.getBody().getId());
            assertEquals("ADMIN", result.getBody().getName());
        }
        
        @Test
        @DisplayName("Should return conflict when role already exists")
        void shouldReturnConflictWhenRoleAlreadyExists() {
            // Given
            CreateRoleRequest request = new CreateRoleRequest("ADMIN");
            
            when(roleHandler.createRole(any(CreateRoleRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).build());
            
            // When
            ResponseEntity<RoleResponse> result = roleController.createRole(request);
            
            // Then
            assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
            assertNull(result.getBody());
        }
    }
    
    @Nested
    @DisplayName("Update Role Tests")
    class UpdateRoleTests {
        
        @Test
        @DisplayName("Should update role successfully")
        void shouldUpdateRoleSuccessfully() {
            // Given
            Long roleId = 1L;
            UpdateRoleRequest request = new UpdateRoleRequest("UPDATED_ADMIN");
            RoleResponse response = new RoleResponse(roleId, "UPDATED_ADMIN", LocalDateTime.now());
            
            when(roleHandler.updateRole(anyLong(), any(UpdateRoleRequest.class)))
                .thenReturn(ResponseEntity.ok(response));
            
            // When
            ResponseEntity<RoleResponse> result = roleController.updateRole(roleId, request);
            
            // Then
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(roleId, result.getBody().getId());
            assertEquals("UPDATED_ADMIN", result.getBody().getName());
        }
        
        @Test
        @DisplayName("Should return not found when role does not exist")
        void shouldReturnNotFoundWhenRoleDoesNotExist() {
            // Given
            Long roleId = 999L;
            UpdateRoleRequest request = new UpdateRoleRequest("NEW_NAME");
            
            when(roleHandler.updateRole(anyLong(), any(UpdateRoleRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());
            
            // When
            ResponseEntity<RoleResponse> result = roleController.updateRole(roleId, request);
            
            // Then
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNull(result.getBody());
        }
    }
    
    @Nested
    @DisplayName("Delete Role Tests")
    class DeleteRoleTests {
        
        @Test
        @DisplayName("Should delete role successfully")
        void shouldDeleteRoleSuccessfully() {
            // Given
            Long roleId = 1L;
            
            when(roleHandler.deleteRole(anyLong()))
                .thenReturn(ResponseEntity.noContent().build());
            
            // When
            ResponseEntity<Void> result = roleController.deleteRole(roleId);
            
            // Then
            assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
            assertNull(result.getBody());
        }
        
        @Test
        @DisplayName("Should return not found when role does not exist")
        void shouldReturnNotFoundWhenRoleDoesNotExist() {
            // Given
            Long roleId = 999L;
            
            when(roleHandler.deleteRole(anyLong()))
                .thenReturn(ResponseEntity.notFound().build());
            
            // When
            ResponseEntity<Void> result = roleController.deleteRole(roleId);
            
            // Then
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        }
    }
    
    @Nested
    @DisplayName("Get Role Tests")
    class GetRoleTests {
        
        @Test
        @DisplayName("Should get role by id successfully")
        void shouldGetRoleByIdSuccessfully() {
            // Given
            Long roleId = 1L;
            RoleResponse response = new RoleResponse(roleId, "ADMIN", LocalDateTime.now());
            
            when(roleHandler.getRoleById(anyLong()))
                .thenReturn(ResponseEntity.ok(response));
            
            // When
            ResponseEntity<RoleResponse> result = roleController.getRoleById(roleId);
            
            // Then
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(roleId, result.getBody().getId());
            assertEquals("ADMIN", result.getBody().getName());
        }
        
        @Test
        @DisplayName("Should return not found when role does not exist")
        void shouldReturnNotFoundWhenRoleDoesNotExist() {
            // Given
            Long roleId = 999L;
            
            when(roleHandler.getRoleById(anyLong()))
                .thenReturn(ResponseEntity.notFound().build());
            
            // When
            ResponseEntity<RoleResponse> result = roleController.getRoleById(roleId);
            
            // Then
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNull(result.getBody());
        }
        
        @Test
        @DisplayName("Should get all roles successfully")
        void shouldGetAllRolesSuccessfully() {
            // Given
            List<RoleResponse> responses = Arrays.asList(
                new RoleResponse(1L, "ADMIN", LocalDateTime.now()),
                new RoleResponse(2L, "USER", LocalDateTime.now())
            );
            
            when(roleHandler.getAllRoles())
                .thenReturn(ResponseEntity.ok(responses));
            
            // When
            ResponseEntity<List<RoleResponse>> result = roleController.getAllRoles();
            
            // Then
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(2, result.getBody().size());
            assertEquals("ADMIN", result.getBody().get(0).getName());
            assertEquals("USER", result.getBody().get(1).getName());
        }
        
        @Test
        @DisplayName("Should search roles successfully")
        void shouldSearchRolesSuccessfully() {
            // Given
            String searchPattern = "ADMIN";
            List<RoleResponse> responses = Arrays.asList(
                new RoleResponse(1L, "ADMIN", LocalDateTime.now()),
                new RoleResponse(2L, "SUPER_ADMIN", LocalDateTime.now())
            );
            
            when(roleHandler.searchRoles(anyString()))
                .thenReturn(ResponseEntity.ok(responses));
            
            // When
            ResponseEntity<List<RoleResponse>> result = roleController.searchRoles(searchPattern);
            
            // Then
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(2, result.getBody().size());
            assertEquals("ADMIN", result.getBody().get(0).getName());
            assertEquals("SUPER_ADMIN", result.getBody().get(1).getName());
        }
        
        @Test
        @DisplayName("Should get role count successfully")
        void shouldGetRoleCountSuccessfully() {
            // Given
            Map<String, Long> countResponse = Map.of("count", 5L);
            
            when(roleHandler.getRoleCount())
                .thenReturn(ResponseEntity.ok(countResponse));
            
            // When
            ResponseEntity<Map<String, Long>> result = roleController.getRoleCount();
            
            // Then
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(5L, result.getBody().get("count"));
        }
    }
}