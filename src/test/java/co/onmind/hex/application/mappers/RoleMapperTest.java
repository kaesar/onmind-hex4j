package co.onmind.hex.application.mappers;

import co.onmind.hex.application.dto.in.CreateRoleRequestDto;
import co.onmind.hex.application.dto.out.RoleResponseDto;
import co.onmind.hex.domain.models.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleMapperTest {

    private final RoleMapper mapper = Mappers.getMapper(RoleMapper.class);

    @Test
    @DisplayName("toResponseDto maps domain to DTO")
    void toResponseDto() {
        LocalDateTime now = LocalDateTime.now();
        Role role = new Role(1L, "ADMIN", now);

        RoleResponseDto dto = mapper.toResponseDto(role);

        assertNotNull(dto);
        assertEquals(1L, dto.id());
        assertEquals("ADMIN", dto.name());
        assertEquals(now, dto.createdAt());
    }

    @Test
    @DisplayName("toResponseDtoList maps collections")
    void toResponseDtoList() {
        List<Role> roles = List.of(
            new Role(1L, "ADMIN", LocalDateTime.now()),
            new Role(2L, "USER", LocalDateTime.now()));

        List<RoleResponseDto> dtos = mapper.toResponseDtoList(roles);

        assertEquals(2, dtos.size());
        assertEquals("ADMIN", dtos.get(0).name());
        assertEquals("USER", dtos.get(1).name());
    }

    @Test
    @DisplayName("toEntity ignores id and createdAt")
    void toEntity() {
        Role role = mapper.toEntity(new CreateRoleRequestDto("EDITOR"));

        assertNotNull(role);
        assertEquals("EDITOR", role.getName());
        assertNull(role.getId());
        assertNull(role.getCreatedAt());
    }
}
