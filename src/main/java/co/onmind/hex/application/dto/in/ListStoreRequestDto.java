package co.onmind.hex.application.dto.in;

import jakarta.validation.constraints.NotBlank;

public record ListStoreRequestDto(
    @NotBlank(message = "Bucket name is required")
    String bucket
) {}
