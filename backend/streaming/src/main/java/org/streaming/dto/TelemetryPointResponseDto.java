package org.streaming.dto;

public record TelemetryPointResponseDto(
        Float x,
        Float y,
        String timestamp,
        Float speed
) {
}
