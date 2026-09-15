package co.onmind.hex.infrastructure.controllers;

import co.onmind.hex.application.dto.in.CreateRoleRequestDto;
import co.onmind.hex.application.dto.in.UpdateRoleRequestDto;
import co.onmind.hex.application.dto.out.RoleResponseDto;
import co.onmind.hex.application.ports.in.CreateRoleTrait;
import co.onmind.hex.application.ports.in.GetRoleTrait;
import co.onmind.hex.application.usecases.RoleUseCase;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Role Controller Tests")
class RoleControllerTest {

    @Mock
    private CreateRoleTrait createRoleTrait;

    @Mock
    private GetRoleTrait getRoleTrait;

    @Mock
    private RoleUseCase roleUseCase;

    private RoleController roleController;

    @BeforeEach
    void setUp() {
        // Explicit construction: RoleUseCase mock also implements the traits,
        // so @InjectMocks constructor resolution would be ambiguous.
        roleController = new RoleController(createRoleTrait, getRoleTrait, roleUseCase);
    }

    @Nested
    @DisplayName("Create Role Tests")
    class CreateRoleTests {

        @Test
        @DisplayName("Should create role successfully")
        void shouldCreateRoleSuccessfully() {
            CreateRoleRequestDto request = new CreateRoleRequestDto("ADMIN");
            RoleResponseDto response = new RoleResponseDto(1L, "ADMIN", LocalDateTime.now());

            when(createRoleTrait.createRole(any(CreateRoleRequestDto.class))).thenReturn(response);

            ResponseEntity<RoleResponseDto> result = roleController.createRole(request);

            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(1L, result.getBody().id());
            assertEquals("ADMIN", result.getBody().name());
        }
    }

    @Nested
    @DisplayName("Update Role Tests")
    class UpdateRoleTests {

        @Test
        @DisplayName("Should update role successfully")
        void shouldUpdateRoleSuccessfully() {
            Long roleId = 1L;
            UpdateRoleRequestDto request = new UpdateRoleRequestDto("UPDATED_ADMIN");
            RoleResponseDto response = new RoleResponseDto(roleId, "UPDATED_ADMIN", LocalDateTime.now());

            when(roleUseCase.updateRole(anyLong(), anyString())).thenReturn(response);

            ResponseEntity<RoleResponseDto> result = roleController.updateRole(roleId, request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(roleId, result.getBody().id());
            assertEquals("UPDATED_ADMIN", result.getBody().name());
        }
    }

    @Nested
    @DisplayName("Delete Role Tests")
    class DeleteRoleTests {

        @Test
        @DisplayName("Should delete role successfully")
        void shouldDeleteRoleSuccessfully() {
            Long roleId = 1L;

            ResponseEntity<Void> result = roleController.deleteRole(roleId);

            assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
            verify(roleUseCase).deleteRole(roleId);
        }
    }

    @Nested
    @DisplayName("Get Role Tests")
    class GetRoleTests {

        @Test
        @DisplayName("Should get role by id successfully")
        void shouldGetRoleByIdSuccessfully() {
            Long roleId = 1L;
            RoleResponseDto response = new RoleResponseDto(roleId, "ADMIN", LocalDateTime.now());

            when(getRoleTrait.getRoleById(anyLong())).thenReturn(response);

            ResponseEntity<RoleResponseDto> result = roleController.getRoleById(roleId);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(roleId, result.getBody().id());
            assertEquals("ADMIN", result.getBody().name());
        }

        @Test
        @DisplayName("Should get all roles successfully")
        void shouldGetAllRolesSuccessfully() {
            List<RoleResponseDto> responses = List.of(
                new RoleResponseDto(1L, "ADMIN", LocalDateTime.now()),
                new RoleResponseDto(2L, "USER", LocalDateTime.now())
            );

            when(getRoleTrait.getAllRoles()).thenReturn(responses);

            ResponseEntity<List<RoleResponseDto>> result = roleController.getAllRoles();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(2, result.getBody().size());
            assertEquals("ADMIN", result.getBody().get(0).name());
            assertEquals("USER", result.getBody().get(1).name());
        }

        @Test
        @DisplayName("Should search roles successfully")
        void shouldSearchRolesSuccessfully() {
            String searchPattern = "ADMIN";
            List<RoleResponseDto> responses = List.of(
                new RoleResponseDto(1L, "ADMIN", LocalDateTime.now()),
                new RoleResponseDto(2L, "SUPER_ADMIN", LocalDateTime.now())
            );

            when(getRoleTrait.getRolesByNamePattern(anyString())).thenReturn(responses);

            ResponseEntity<List<RoleResponseDto>> result = roleController.searchRoles(searchPattern);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(2, result.getBody().size());
        }

        @Test
        @DisplayName("Should get role count successfully")
        void shouldGetRoleCountSuccessfully() {
            when(roleUseCase.countRoles()).thenReturn(5L);

            ResponseEntity<Map<String, Long>> result = roleController.getRoleCount();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(5L, result.getBody().get("count"));
        }
    }
}
