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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleHandler.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Role Handler Tests")
class RoleHandlerTest {
    
    @Mock
    private RoleServicePort roleServicePort;
    
    @Mock
    private RoleMapper roleMapper;
    
    private RoleHandler roleHandler;
    
    @BeforeEach
    void setUp() {
        roleHandler = new RoleHandler(roleServicePort, roleMapper);
    }
    
    @Nested
    @DisplayName("Create Role Tests")
    class CreateRoleTests {
        
        @Test
        @DisplayName("Should create role successfully")
        void shouldCreateRoleSuccessfully() {
            // Given
            CreateRoleRequest request = new CreateRoleRequest("ADMIN");
            Role createdRole = new Role(1L, "ADMIN", LocalDateTime.now());
            RoleResponse expectedResponse = new RoleResponse(1L, "ADMIN", LocalDateTime.now());
            
            when(roleServicePort.createRole("ADMIN")).thenReturn(createdRole);
            when(roleMapper.toResponse(createdRole)).thenReturn(expectedResponse);
            
            // When
            ResponseEntity<RoleResponse> response = roleHandler.createRole(request);
            
            // Then
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("ADMIN", response.getBody().getName());
            
            verify(roleServicePort).createRole("ADMIN");
            verify(roleMapper).toResponse(createdRole);
        }
        
        @Test
        @DisplayName("Should return conflict when role already exists")
        void shouldReturnConflictWhenRoleAlreadyExists() {
            // Given
            CreateRoleRequest request = new CreateRoleRequest("ADMIN");
            
            when(roleServicePort.createRole("ADMIN")).thenThrow(new RoleAlreadyExistsException("Role already exists"));
            
            // When
            ResponseEntity<RoleResponse> response = roleHandler.createRole(request);
            
            // Then
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNull(response.getBody());
            
            verify(roleServicePort).createRole("ADMIN");
            verifyNoInteractions(roleMapper);
        }
        
        @Test
        @DisplayName("Should return bad request for invalid input")
        void shouldReturnBadRequestForInvalidInput() {
            // Given
            CreateRoleRequest request = new CreateRoleRequest("");
            
            when(roleServicePort.createRole("")).thenThrow(new IllegalArgumentException("Invalid name"));
            
            // When
            ResponseEntity<RoleResponse> response = roleHandler.createRole(request);
            
            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNull(response.getBody());
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
            Role updatedRole = new Role(roleId, "UPDATED_ADMIN", LocalDateTime.now());
            RoleResponse expectedResponse = new RoleResponse(roleId, "UPDATED_ADMIN", LocalDateTime.now());
            
            when(roleServicePort.updateRole(roleId, "UPDATED_ADMIN")).thenReturn(updatedRole);
            when(roleMapper.toResponse(updatedRole)).thenReturn(expectedResponse);
            
            // When
            ResponseEntity<RoleResponse> response = roleHandler.updateRole(roleId, request);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("UPDATED_ADMIN", response.getBody().getName());
        }
        
        @Test
        @DisplayName("Should return not found when role does not exist")
        void shouldReturnNotFoundWhenRoleDoesNotExist() {
            // Given
            Long roleId = 999L;
            UpdateRoleRequest request = new UpdateRoleRequest("NEW_NAME");
            
            when(roleServicePort.updateRole(roleId, "NEW_NAME")).thenThrow(new RoleNotFoundException("Role not found"));
            
            // When
            ResponseEntity<RoleResponse> response = roleHandler.updateRole(roleId, request);
            
            // Then
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
        }
        
        @Test
        @DisplayName("Should return forbidden when trying to update system role")
        void shouldReturnForbiddenWhenTryingToUpdateSystemRole() {
            // Given
            Long roleId = 1L;
            UpdateRoleRequest request = new UpdateRoleRequest("NEW_NAME");
            
            when(roleServicePort.updateRole(roleId, "NEW_NAME")).thenThrow(new SystemRoleException("Cannot update system role"));
            
            // When
            ResponseEntity<RoleResponse> response = roleHandler.updateRole(roleId, request);
            
            // Then
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNull(response.getBody());
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
            
            doNothing().when(roleServicePort).deleteRole(roleId);
            
            // When
            ResponseEntity<Void> response = roleHandler.deleteRole(roleId);
            
            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            
            verify(roleServicePort).deleteRole(roleId);
        }
        
        @Test
        @DisplayName("Should return not found when role does not exist")
        void shouldReturnNotFoundWhenRoleDoesNotExist() {
            // Given
            Long roleId = 999L;
            
            doThrow(new RoleNotFoundException("Role not found")).when(roleServicePort).deleteRole(roleId);
            
            // When
            ResponseEntity<Void> response = roleHandler.deleteRole(roleId);
            
            // Then
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should return forbidden when trying to delete system role")
        void shouldReturnForbiddenWhenTryingToDeleteSystemRole() {
            // Given
            Long roleId = 1L;
            
            doThrow(new SystemRoleException("Cannot delete system role")).when(roleServicePort).deleteRole(roleId);
            
            // When
            ResponseEntity<Void> response = roleHandler.deleteRole(roleId);
            
            // Then
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
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
            Role role = new Role(roleId, "ADMIN", LocalDateTime.now());
            RoleResponse expectedResponse = new RoleResponse(roleId, "ADMIN", LocalDateTime.now());
            
            when(roleServicePort.getRoleById(roleId)).thenReturn(Optional.of(role));
            when(roleMapper.toResponse(role)).thenReturn(expectedResponse);
            
            // When
            ResponseEntity<RoleResponse> response = roleHandler.getRoleById(roleId);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("ADMIN", response.getBody().getName());
        }
        
        @Test
        @DisplayName("Should return not found when role does not exist")
        void shouldReturnNotFoundWhenRoleDoesNotExist() {
            // Given
            Long roleId = 999L;
            
            when(roleServicePort.getRoleById(roleId)).thenReturn(Optional.empty());
            
            // When
            ResponseEntity<RoleResponse> response = roleHandler.getRoleById(roleId);
            
            // Then
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
            
            verifyNoInteractions(roleMapper);
        }
        
        @Test
        @DisplayName("Should get all roles successfully")
        void shouldGetAllRolesSuccessfully() {
            // Given
            List<Role> roles = Arrays.asList(
                new Role(1L, "ADMIN", LocalDateTime.now()),
                new Role(2L, "USER", LocalDateTime.now())
            );
            List<RoleResponse> expectedResponses = Arrays.asList(
                new RoleResponse(1L, "ADMIN", LocalDateTime.now()),
                new RoleResponse(2L, "USER", LocalDateTime.now())
            );
            
            when(roleServicePort.getAllRoles()).thenReturn(roles);
            when(roleMapper.toResponseList(roles)).thenReturn(expectedResponses);
            
            // When
            ResponseEntity<List<RoleResponse>> response = roleHandler.getAllRoles();
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
        }
        
        @Test
        @DisplayName("Should search roles successfully")
        void shouldSearchRolesSuccessfully() {
            // Given
            String searchPattern = "ADMIN";
            List<Role> roles = Arrays.asList(new Role(1L, "ADMIN", LocalDateTime.now()));
            List<RoleResponse> expectedResponses = Arrays.asList(new RoleResponse(1L, "ADMIN", LocalDateTime.now()));
            
            when(roleServicePort.searchRolesByName(searchPattern)).thenReturn(roles);
            when(roleMapper.toResponseList(roles)).thenReturn(expectedResponses);
            
            // When
            ResponseEntity<List<RoleResponse>> response = roleHandler.searchRoles(searchPattern);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
        }
        
        @Test
        @DisplayName("Should get role count successfully")
        void shouldGetRoleCountSuccessfully() {
            // Given
            Long expectedCount = 5L;
            
            when(roleServicePort.getRoleCount()).thenReturn(expectedCount);
            
            // When
            ResponseEntity<Map<String, Long>> response = roleHandler.getRoleCount();
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(expectedCount, response.getBody().get("count"));
        }
    }
}