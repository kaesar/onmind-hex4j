package co.onmind.hex.application.usecases;

import co.onmind.hex.application.dto.in.CreateRoleRequestDto;
import co.onmind.hex.application.dto.out.RoleResponseDto;
import co.onmind.hex.application.mappers.RoleMapper;
import co.onmind.hex.application.ports.out.RoleRepositoryPort;
import co.onmind.hex.domain.exceptions.DuplicateRoleException;
import co.onmind.hex.domain.exceptions.RoleNotFoundException;
import co.onmind.hex.domain.models.Role;
import co.onmind.hex.domain.services.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleUseCaseTest {

    @Mock private RoleRepositoryPort roleRepository;
    @Mock private RoleMapper roleMapper;

    private RoleService roleService;
    private RoleUseCase useCase;

    @BeforeEach
    void setUp() {
        roleService = new RoleService();
        useCase = new RoleUseCase(roleService, roleRepository, roleMapper);
    }

    @Test
    @DisplayName("createRole persists and maps to DTO")
    void createRoleOk() {
        when(roleRepository.existsByName("EDITOR")).thenReturn(false);
        Role created = new Role("EDITOR");
        Role saved = new Role(1L, "EDITOR", LocalDateTime.now());
        when(roleRepository.save(any(Role.class))).thenReturn(saved);
        RoleResponseDto dto = new RoleResponseDto(1L, "EDITOR", saved.getCreatedAt());
        when(roleMapper.toResponseDto(saved)).thenReturn(dto);

        RoleResponseDto result = useCase.createRole(new CreateRoleRequestDto("EDITOR"));

        assertEquals(1L, result.id());
        assertEquals("EDITOR", result.name());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("createRole rejects duplicates")
    void createRoleDuplicate() {
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        assertThrows(DuplicateRoleException.class,
            () -> useCase.createRole(new CreateRoleRequestDto("ADMIN")));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("createRole rejects null request")
    void createRoleNull() {
        assertThrows(IllegalArgumentException.class, () -> useCase.createRole(null));
    }

    @Test
    @DisplayName("getRoleById returns mapped DTO")
    void getRoleByIdOk() {
        Role role = new Role(1L, "ADMIN", LocalDateTime.now());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleMapper.toResponseDto(role)).thenReturn(new RoleResponseDto(1L, "ADMIN", role.getCreatedAt()));

        RoleResponseDto result = useCase.getRoleById(1L);

        assertEquals("ADMIN", result.name());
    }

    @Test
    @DisplayName("getRoleById throws when missing")
    void getRoleByIdMissing() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> useCase.getRoleById(999L));
    }

    @Test
    @DisplayName("getRoleById validates id")
    void getRoleByIdInvalid() {
        assertThrows(IllegalArgumentException.class, () -> useCase.getRoleById(null));
        assertThrows(IllegalArgumentException.class, () -> useCase.getRoleById(0L));
    }

    @Test
    @DisplayName("getAllRoles maps list")
    void getAllRoles() {
        List<Role> roles = List.of(new Role(1L, "ADMIN", LocalDateTime.now()));
        when(roleRepository.findAll()).thenReturn(roles);
        when(roleMapper.toResponseDtoList(roles))
            .thenReturn(List.of(new RoleResponseDto(1L, "ADMIN", LocalDateTime.now())));

        assertEquals(1, useCase.getAllRoles().size());
    }

    @Test
    @DisplayName("getRolesByNamePattern validates pattern")
    void searchPatternInvalid() {
        assertThrows(IllegalArgumentException.class, () -> useCase.getRolesByNamePattern("  "));
        assertThrows(IllegalArgumentException.class, () -> useCase.getRolesByNamePattern(null));
    }

    @Test
    @DisplayName("updateRole updates and saves")
    void updateRoleOk() {
        Role existing = new Role(1L, "USER", LocalDateTime.now());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roleRepository.findByName("EDITOR")).thenReturn(Optional.empty());
        Role saved = new Role(1L, "EDITOR", existing.getCreatedAt());
        when(roleRepository.save(any(Role.class))).thenReturn(saved);
        when(roleMapper.toResponseDto(saved))
            .thenReturn(new RoleResponseDto(1L, "EDITOR", saved.getCreatedAt()));

        RoleResponseDto result = useCase.updateRole(1L, "EDITOR");

        assertEquals("EDITOR", result.name());
    }

    @Test
    @DisplayName("updateRole rejects system roles")
    void updateRoleSystem() {
        Role system = new Role(1L, "SYSTEM_ADMIN", LocalDateTime.now());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(system));

        assertThrows(IllegalArgumentException.class, () -> useCase.updateRole(1L, "OTHER"));
    }

    @Test
    @DisplayName("deleteRole deletes existing role")
    void deleteRoleOk() {
        Role existing = new Role(1L, "USER", LocalDateTime.now());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(existing));

        useCase.deleteRole(1L);

        verify(roleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteRole throws when missing")
    void deleteRoleMissing() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> useCase.deleteRole(999L));
    }

    @Test
    @DisplayName("countRoles delegates to repository")
    void countRoles() {
        when(roleRepository.count()).thenReturn(3L);
        assertEquals(3L, useCase.countRoles());
    }
}
