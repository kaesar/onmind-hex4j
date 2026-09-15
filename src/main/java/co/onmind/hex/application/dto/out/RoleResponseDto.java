package co.onmind.hex.application.dto.out;

import java.time.LocalDateTime;

public record RoleResponseDto(
    Long id,
    String name,
    LocalDateTime createdAt
) {}
