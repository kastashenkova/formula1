package org.streaming.dto;

public record TelemetryPointResponseDto(
        Integer x,
        Integer y,
        String timestamp
) {
}
