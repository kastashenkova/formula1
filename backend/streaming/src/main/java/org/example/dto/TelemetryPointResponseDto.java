package org.example.dto;

public record TelemetryPointResponseDto(
        Float x,
        Float y,
        String timestamp,
        Float speed
) {
}
