package co.onmind.hex.application.ports.in;

import co.onmind.hex.application.dto.in.CreateRoleRequestDto;
import co.onmind.hex.application.dto.out.RoleResponseDto;

public interface CreateRoleTrait {

    RoleResponseDto createRole(CreateRoleRequestDto request);
}
