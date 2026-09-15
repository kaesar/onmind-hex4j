package co.onmind.hex.application.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KafkaScriptCommand(
    @JsonProperty("script") String script,
    @JsonProperty("correlationId") String correlationId
) {}
