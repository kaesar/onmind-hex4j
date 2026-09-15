package co.onmind.hex.application.dto.out;

import java.time.LocalDateTime;

public record StoreItemResponseDto(
    String key,
    Long size,
    LocalDateTime lastModified,
    String eTag
) {}
