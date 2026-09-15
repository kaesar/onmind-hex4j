package co.onmind.hex.application.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequestDto(
    @NotBlank(message = "Role name cannot be blank")
    @Size(min = 1, max = 100, message = "Role name must be between 1 and 100 characters")
    String name
) {}
