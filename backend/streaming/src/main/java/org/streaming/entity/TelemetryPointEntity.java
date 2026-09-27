package org.streaming.entity;

public record TelemetryPointEntity(
        Float x,
        Float y,
        String timestamp,
        Float speed
) {
}
