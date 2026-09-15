package co.onmind.hex.application.dto.in;

import jakarta.validation.constraints.NotBlank;

public record ExecuteScriptRequestDto(
    @NotBlank(message = "Script file name is required")
    String script
) {}
