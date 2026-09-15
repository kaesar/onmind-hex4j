package co.onmind.hex.application.mappers;

import co.onmind.hex.application.dto.in.CreateRoleRequestDto;
import co.onmind.hex.application.dto.out.RoleResponseDto;
import co.onmind.hex.domain.models.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponseDto toResponseDto(Role role);

    List<RoleResponseDto> toResponseDtoList(List<Role> roles);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "withName", ignore = true)
    Role toEntity(CreateRoleRequestDto dto);
}
