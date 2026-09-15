package co.onmind.hex.application.ports.in;

import co.onmind.hex.application.dto.out.RoleResponseDto;

import java.util.List;

public interface GetRoleTrait {

    RoleResponseDto getRoleById(Long id);

    List<RoleResponseDto> getAllRoles();

    List<RoleResponseDto> getRolesByNamePattern(String namePattern);
}
