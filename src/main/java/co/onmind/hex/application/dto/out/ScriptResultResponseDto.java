package co.onmind.hex.application.dto.out;

public record ScriptResultResponseDto(
    Object value,
    String stdout,
    String stderr
) {}
