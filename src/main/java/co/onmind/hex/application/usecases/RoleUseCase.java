package co.onmind.hex.application.usecases;

import co.onmind.hex.application.dto.in.CreateRoleRequestDto;
import co.onmind.hex.application.dto.out.RoleResponseDto;
import co.onmind.hex.application.mappers.RoleMapper;
import co.onmind.hex.application.ports.in.CreateRoleTrait;
import co.onmind.hex.application.ports.in.GetRoleTrait;
import co.onmind.hex.application.ports.out.RoleRepositoryPort;
import co.onmind.hex.domain.exceptions.DuplicateRoleException;
import co.onmind.hex.domain.exceptions.RoleNotFoundException;
import co.onmind.hex.domain.models.Role;
import co.onmind.hex.domain.services.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleUseCase implements CreateRoleTrait, GetRoleTrait {

    private final RoleService roleService;
    private final RoleRepositoryPort roleRepository;
    private final RoleMapper roleMapper;

    public RoleUseCase(
            RoleService roleService,
            RoleRepositoryPort roleRepository,
            RoleMapper roleMapper) {
        this.roleService = roleService;
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public RoleResponseDto createRole(CreateRoleRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateRoleRequestDto cannot be null");
        }

        validateUniqueRoleName(request.name());
        Role role = roleService.createRole(request.name());
        Role saved = roleRepository.save(role);
        return roleMapper.toResponseDto(saved);
    }

    @Override
    public RoleResponseDto getRoleById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        if (id <= 0) {
            throw new IllegalArgumentException("Role ID must be a positive number");
        }

        return roleRepository.findById(id)
            .map(roleMapper::toResponseDto)
            .orElseThrow(() -> RoleNotFoundException.forId(id));
    }

    @Override
    public List<RoleResponseDto> getAllRoles() {
        return roleMapper.toResponseDtoList(roleRepository.findAll());
    }

    @Override
    public List<RoleResponseDto> getRolesByNamePattern(String namePattern) {
        if (namePattern == null || namePattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Name pattern cannot be null or empty");
        }

        return roleMapper.toResponseDtoList(
            roleRepository.findByNameContainingIgnoreCase(namePattern.trim()));
    }

    public RoleResponseDto updateRole(Long id, String newName) {
        if (id == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        Role existing = roleRepository.findById(id)
            .orElseThrow(() -> RoleNotFoundException.forId(id));

        if (existing.isSystemRole()) {
            throw new IllegalArgumentException("Cannot update system role: " + existing.getName());
        }

        roleRepository.findByName(newName.trim().toUpperCase()).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw DuplicateRoleException.forName(newName.trim());
            }
        });

        Role updated = roleService.updateRole(existing, newName);
        return roleMapper.toResponseDto(roleRepository.save(updated));
    }

    public void deleteRole(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        Role existing = roleRepository.findById(id)
            .orElseThrow(() -> RoleNotFoundException.forId(id));

        roleService.validateRoleDeletion(existing);
        roleRepository.deleteById(id);
    }

    public long countRoles() {
        return roleRepository.count();
    }

    private void validateUniqueRoleName(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }

        if (roleRepository.existsByName(roleName.trim())) {
            throw DuplicateRoleException.forName(roleName.trim());
        }
    }
}
