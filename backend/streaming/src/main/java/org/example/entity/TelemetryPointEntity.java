package org.example.entity;

public record TelemetryPointEntity(
        Float x,
        Float y,
        String timestamp,
        Float speed
) {
}
