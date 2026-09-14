package org.example.dto.telemetry_point;

public record TelemetryPointResponseDto(
        Float x,
        Float y,
        String timestamp,
        Float speed
) {
}
