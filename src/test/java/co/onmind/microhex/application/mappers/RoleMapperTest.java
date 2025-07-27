package co.onmind.microhex.application.mappers;

import co.onmind.microhex.application.dto.in.CreateRoleRequestDto;
import co.onmind.microhex.application.dto.out.RoleResponseDto;
import co.onmind.microhex.domain.models.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RoleMapper.
 * 
 * This test class verifies the correct mapping behavior between
 * Role domain models and DTOs using MapStruct generated implementation.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@SpringBootTest(classes = {RoleMapperImpl.class})
class RoleMapperTest {

    @Autowired
    private RoleMapper roleMapper;

    @Test
    void shouldMapRoleToResponseDto() {
        // Given
        Long expectedId = 1L;
        String expectedName = "ADMIN";
        LocalDateTime expectedCreatedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        
        Role role = new Role(expectedId, expectedName, expectedCreatedAt);

        // When
        RoleResponseDto responseDto = roleMapper.toResponseDto(role);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.id()).isEqualTo(expectedId);
        assertThat(responseDto.name()).isEqualTo(expectedName);
        assertThat(responseDto.createdAt()).isEqualTo(expectedCreatedAt);
    }

    @Test
    void shouldMapCreateRoleRequestDtoToDomainModel() {
        // Given
        String expectedName = "USER";
        CreateRoleRequestDto requestDto = new CreateRoleRequestDto(expectedName);

        // When
        Role role = roleMapper.toDomainModel(requestDto);

        // Then
        assertThat(role).isNotNull();
        assertThat(role.getName()).isEqualTo(expectedName);
        assertThat(role.getId()).isNull(); // Should be ignored in mapping
        assertThat(role.getCreatedAt()).isNull(); // Should be ignored in mapping
    }

    @Test
    void shouldHandleNullRoleWhenMappingToResponseDto() {
        // Given
        Role role = null;

        // When
        RoleResponseDto responseDto = roleMapper.toResponseDto(role);

        // Then
        assertThat(responseDto).isNull();
    }

    @Test
    void shouldHandleNullCreateRoleRequestDtoWhenMappingToDomainModel() {
        // Given
        CreateRoleRequestDto requestDto = null;

        // When
        Role role = roleMapper.toDomainModel(requestDto);

        // Then
        assertThat(role).isNull();
    }

    @Test
    void shouldMapRoleWithNullFieldsToResponseDto() {
        // Given
        Role role = new Role();
        role.setId(null);
        role.setName(null);
        role.setCreatedAt(null);

        // When
        RoleResponseDto responseDto = roleMapper.toResponseDto(role);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.id()).isNull();
        assertThat(responseDto.name()).isNull();
        assertThat(responseDto.createdAt()).isNull();
    }

    @Test
    void shouldMapCreateRoleRequestDtoWithEmptyNameToDomainModel() {
        // Given
        String emptyName = "";
        CreateRoleRequestDto requestDto = new CreateRoleRequestDto(emptyName);

        // When
        Role role = roleMapper.toDomainModel(requestDto);

        // Then
        assertThat(role).isNotNull();
        assertThat(role.getName()).isEqualTo(emptyName);
        assertThat(role.getId()).isNull();
        assertThat(role.getCreatedAt()).isNull();
    }
}