package co.onmind.microhex.application.usecases;

import co.onmind.microhex.application.dto.in.CreateRoleRequestDto;
import co.onmind.microhex.application.dto.out.RoleResponseDto;
import co.onmind.microhex.application.mappers.RoleMapper;
import co.onmind.microhex.application.ports.out.RoleRepositoryPort;
import co.onmind.microhex.domain.exceptions.DuplicateRoleException;
import co.onmind.microhex.domain.exceptions.RoleNotFoundException;
import co.onmind.microhex.domain.models.Role;
import co.onmind.microhex.domain.services.RoleService;
import co.onmind.microhex.infrastructure.webclients.NotificationWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleUseCase.
 * 
 * This test class verifies the behavior of the role use case implementation,
 * including successful operations, error handling, and edge cases.
 * Tests use mocks to isolate the use case logic from its dependencies.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleUseCase Tests")
class RoleUseCaseImplTest {
    
    @Mock
    private RoleService roleService;
    
    @Mock
    private RoleRepositoryPort roleRepositoryPort;
    
    @Mock
    private RoleMapper roleMapper;
    
    @Mock
    private NotificationWebClient notificationWebClient;

    private RoleUseCase roleUseCase;
    
    @BeforeEach
    void setUp() {
        roleUseCase = new RoleUseCase(roleService, roleRepositoryPort, roleMapper, notificationWebClient);
    }
    
    @Nested
    @DisplayName("Create Role Tests")
    class CreateRoleTests {
        
        @Test
        @DisplayName("Should create role successfully when valid request is provided")
        void shouldCreateRoleSuccessfully() {
            // Given
            String roleName = "ADMIN";
            CreateRoleRequestDto request = new CreateRoleRequestDto(roleName);
            
            Role domainRole = new Role();
            domainRole.setName(roleName);
            domainRole.setCreatedAt(LocalDateTime.now());
            
            Role savedRole = new Role();
            savedRole.setId(1L);
            savedRole.setName(roleName);
            savedRole.setCreatedAt(LocalDateTime.now());
            
            RoleResponseDto expectedResponse = new RoleResponseDto(1L, roleName, savedRole.getCreatedAt());
            
            when(roleRepositoryPort.existsByName(roleName)).thenReturn(false);
            when(roleService.createRole(roleName)).thenReturn(domainRole);
            when(roleRepositoryPort.save(domainRole)).thenReturn(savedRole);
            when(roleMapper.toResponseDto(savedRole)).thenReturn(expectedResponse);
            
            // When
            RoleResponseDto result = roleUseCase.createRole(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo(roleName);
            assertThat(result.createdAt()).isNotNull();
            
            verify(roleRepositoryPort).existsByName(roleName);
            verify(roleService).createRole(roleName);
            verify(roleRepositoryPort).save(domainRole);
            verify(roleMapper).toResponseDto(savedRole);
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            // When & Then
            assertThatThrownBy(() -> roleUseCase.createRole(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Create role request cannot be null");
            
            verifyNoInteractions(roleService, roleRepositoryPort, roleMapper);
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException when role name is null")
        void shouldThrowExceptionWhenRoleNameIsNull() {
            // Given
            CreateRoleRequestDto request = new CreateRoleRequestDto(null);
            
            // When & Then
            assertThatThrownBy(() -> roleUseCase.createRole(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Role name cannot be null or empty");
            
            verifyNoInteractions(roleService, roleRepositoryPort, roleMapper);
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException when role name is empty")
        void shouldThrowExceptionWhenRoleNameIsEmpty() {
            // Given
            CreateRoleRequestDto request = new CreateRoleRequestDto("");
            
            // When & Then
            assertThatThrownBy(() -> roleUseCase.createRole(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Role name cannot be null or empty");
            
            verifyNoInteractions(roleService, roleRepositoryPort, roleMapper);
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException when role name is blank")
        void shouldThrowExceptionWhenRoleNameIsBlank() {
            // Given
            CreateRoleRequestDto request = new CreateRoleRequestDto("   ");
            
            // When & Then
            assertThatThrownBy(() -> roleUseCase.createRole(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Role name cannot be null or empty");
            
            verifyNoInteractions(roleService, roleRepositoryPort, roleMapper);
        }
        
        @Test
        @DisplayName("Should throw DuplicateRoleException when role name already exists")
        void shouldThrowDuplicateRoleExceptionWhenRoleExists() {
            // Given
            String roleName = "ADMIN";
            CreateRoleRequestDto request = new CreateRoleRequestDto(roleName);
            
            when(roleRepositoryPort.existsByName(roleName)).thenReturn(true);
            
            // When & Then
            assertThatThrownBy(() -> roleUseCase.createRole(request))
                    .isInstanceOf(DuplicateRoleException.class)
                    .hasMessage("Role with name 'ADMIN' already exists");
            
            verify(roleRepositoryPort).existsByName(roleName);
            verifyNoInteractions(roleService, roleMapper);
            verify(roleRepositoryPort, never()).save(any());
        }
        
        @Test
        @DisplayName("Should trim role name before processing")
        void shouldTrimRoleNameBeforeProcessing() {
            // Given
            String roleNameWithSpaces = "  ADMIN  ";
            String trimmedRoleName = "ADMIN";
            CreateRoleRequestDto request = new CreateRoleRequestDto(roleNameWithSpaces);
            
            Role domainRole = new Role();
            domainRole.setName(trimmedRoleName);
            domainRole.setCreatedAt(LocalDateTime.now());
            
            Role savedRole = new Role();
            savedRole.setId(1L);
            savedRole.setName(trimmedRoleName);
            savedRole.setCreatedAt(LocalDateTime.now());
            
            RoleResponseDto expectedResponse = new RoleResponseDto(1L, trimmedRoleName, savedRole.getCreatedAt());
            
            when(roleRepositoryPort.existsByName(trimmedRoleName)).thenReturn(false);
            when(roleService.createRole(trimmedRoleName)).thenReturn(domainRole);
            when(roleRepositoryPort.save(domainRole)).thenReturn(savedRole);
            when(roleMapper.toResponseDto(savedRole)).thenReturn(expectedResponse);
            
            // When
            RoleResponseDto result = roleUseCase.createRole(request);
            
            // Then
            assertThat(result.name()).isEqualTo(trimmedRoleName);
            verify(roleRepositoryPort).existsByName(trimmedRoleName);
            verify(roleService).createRole(trimmedRoleName);
        }
        
        @Test
        @DisplayName("Should propagate domain service exceptions")
        void shouldPropagateDomainServiceExceptions() {
            // Given
            String roleName = "INVALID";
            CreateRoleRequestDto request = new CreateRoleRequestDto(roleName);
            
            when(roleRepositoryPort.existsByName(roleName)).thenReturn(false);
            when(roleService.createRole(roleName)).thenThrow(new IllegalArgumentException("Invalid role name"));
            
            // When & Then
            assertThatThrownBy(() -> roleUseCase.createRole(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid role name");
            
            verify(roleRepositoryPort).existsByName(roleName);
            verify(roleService).createRole(roleName);
            verify(roleRepositoryPort, never()).save(any());
            verifyNoInteractions(roleMapper);
        }
    }
    
    @Nested
    @DisplayName("Get Role By ID Tests")
    class GetRoleByIdTests {
        
        @Test
        @DisplayName("Should return role when valid ID is provided")
        void shouldReturnRoleWhenValidIdProvided() {
            // Given
            Long roleId = 1L;
            Role role = new Role();
            role.setId(roleId);
            role.setName("ADMIN");
            role.setCreatedAt(LocalDateTime.now());
            
            RoleResponseDto expectedResponse = new RoleResponseDto(roleId, "ADMIN", role.getCreatedAt());
            
            when(roleRepositoryPort.findById(roleId)).thenReturn(Optional.of(role));
            when(roleMapper.toResponseDto(role)).thenReturn(expectedResponse);
            
            // When
            RoleResponseDto result = roleUseCase.getRoleById(roleId);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(roleId);
            assertThat(result.name()).isEqualTo("ADMIN");
            
            verify(roleRepositoryPort).findById(roleId);
            verify(roleMapper).toResponseDto(role);
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException when ID is null")
        void shouldThrowExceptionWhenIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> roleUseCase.getRoleById(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Role ID cannot be null");
            
            verifyNoInteractions(roleRepositoryPort, roleMapper);
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException when ID is zero")
        void shouldThrowExceptionWhenIdIsZero() {
            // When & Then
            assertThatThrownBy(() -> roleUseCase.getRoleById(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Role ID must be positive");
            
            verifyNoInteractions(roleRepositoryPort, roleMapper);
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException when ID is negative")
        void shouldThrowExceptionWhenIdIsNegative() {
            // When & Then
            assertThatThrownBy(() -> roleUseCase.getRoleById(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Role ID must be positive");
            
            verifyNoInteractions(roleRepositoryPort, roleMapper);
        }
        
        @Test
        @DisplayName("Should throw RoleNotFoundException when role does not exist")
        void shouldThrowRoleNotFoundExceptionWhenRoleDoesNotExist() {
            // Given
            Long roleId = 999L;
            when(roleRepositoryPort.findById(roleId)).thenReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> roleUseCase.getRoleById(roleId))
                    .isInstanceOf(RoleNotFoundException.class)
                    .hasMessage("Role with ID 999 not found");
            
            verify(roleRepositoryPort).findById(roleId);
            verifyNoInteractions(roleMapper);
        }
    }
    
    @Nested
    @DisplayName("Get All Roles Tests")
    class GetAllRolesTests {
        
        @Test
        @DisplayName("Should return all roles when roles exist")
        void shouldReturnAllRolesWhenRolesExist() {
            // Given
            Role role1 = new Role();
            role1.setId(1L);
            role1.setName("ADMIN");
            role1.setCreatedAt(LocalDateTime.now());
            
            Role role2 = new Role();
            role2.setId(2L);
            role2.setName("USER");
            role2.setCreatedAt(LocalDateTime.now());
            
            List<Role> roles = Arrays.asList(role1, role2);
            
            RoleResponseDto response1 = new RoleResponseDto(1L, "ADMIN", role1.getCreatedAt());
            RoleResponseDto response2 = new RoleResponseDto(2L, "USER", role2.getCreatedAt());
            
            when(roleRepositoryPort.findAll()).thenReturn(roles);
            when(roleMapper.toResponseDto(role1)).thenReturn(response1);
            when(roleMapper.toResponseDto(role2)).thenReturn(response2);
            
            // When
            List<RoleResponseDto> result = roleUseCase.getAllRoles();
            
            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("ADMIN");
            assertThat(result.get(1).name()).isEqualTo("USER");
            
            verify(roleRepositoryPort).findAll();
            verify(roleMapper).toResponseDto(role1);
            verify(roleMapper).toResponseDto(role2);
        }
        
        @Test
        @DisplayName("Should return empty list when no roles exist")
        void shouldReturnEmptyListWhenNoRolesExist() {
            // Given
            when(roleRepositoryPort.findAll()).thenReturn(Collections.emptyList());
            
            // When
            List<RoleResponseDto> result = roleUseCase.getAllRoles();
            
            // Then
            assertThat(result).isEmpty();
            
            verify(roleRepositoryPort).findAll();
            verifyNoInteractions(roleMapper);
        }
    }
    
    @Nested
    @DisplayName("Exists By Name Tests")
    class ExistsByNameTests {
        
        @Test
        @DisplayName("Should return true when role exists with given name")
        void shouldReturnTrueWhenRoleExistsWithGivenName() {
            // Given
            String roleName = "ADMIN";
            when(roleService.isValidRoleName(roleName)).thenReturn(true);
            when(roleRepositoryPort.existsByName(roleName)).thenReturn(true);
            
            // When
            boolean result = roleUseCase.existsByName(roleName);
            
            // Then
            assertThat(result).isTrue();
            
            verify(roleService).isValidRoleName(roleName);
            verify(roleRepositoryPort).existsByName(roleName);
        }
        
        @Test
        @DisplayName("Should return false when role does not exist with given name")
        void shouldReturnFalseWhenRoleDoesNotExistWithGivenName() {
            // Given
            String roleName = "NONEXISTENT";
            when(roleService.isValidRoleName(roleName)).thenReturn(true);
            when(roleRepositoryPort.existsByName(roleName)).thenReturn(false);
            
            // When
            boolean result = roleUseCase.existsByName(roleName);
            
            // Then
            assertThat(result).isFalse();
            
            verify(roleService).isValidRoleName(roleName);
            verify(roleRepositoryPort).existsByName(roleName);
        }
        
        @Test
        @DisplayName("Should return false when role name is invalid")
        void shouldReturnFalseWhenRoleNameIsInvalid() {
            // Given
            String invalidRoleName = "INVALID@NAME";
            when(roleService.isValidRoleName(invalidRoleName)).thenReturn(false);
            
            // When
            boolean result = roleUseCase.existsByName(invalidRoleName);
            
            // Then
            assertThat(result).isFalse();
            
            verify(roleService).isValidRoleName(invalidRoleName);
            verifyNoInteractions(roleRepositoryPort);
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            // When & Then
            assertThatThrownBy(() -> roleUseCase.existsByName(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Role name cannot be null or empty");
            
            verifyNoInteractions(roleService, roleRepositoryPort);
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException when name is empty")
        void shouldThrowExceptionWhenNameIsEmpty() {
            // When & Then
            assertThatThrownBy(() -> roleUseCase.existsByName(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Role name cannot be null or empty");
            
            verifyNoInteractions(roleService, roleRepositoryPort);
        }
        
        @Test
        @DisplayName("Should throw IllegalArgumentException when name is blank")
        void shouldThrowExceptionWhenNameIsBlank() {
            // When & Then
            assertThatThrownBy(() -> roleUseCase.existsByName("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Role name cannot be null or empty");
            
            verifyNoInteractions(roleService, roleRepositoryPort);
        }
        
        @Test
        @DisplayName("Should trim name before validation")
        void shouldTrimNameBeforeValidation() {
            // Given
            String nameWithSpaces = "  ADMIN  ";
            String trimmedName = "ADMIN";
            when(roleService.isValidRoleName(trimmedName)).thenReturn(true);
            when(roleRepositoryPort.existsByName(trimmedName)).thenReturn(true);
            
            // When
            boolean result = roleUseCase.existsByName(nameWithSpaces);
            
            // Then
            assertThat(result).isTrue();
            
            verify(roleService).isValidRoleName(trimmedName);
            verify(roleRepositoryPort).existsByName(trimmedName);
        }
    }
}