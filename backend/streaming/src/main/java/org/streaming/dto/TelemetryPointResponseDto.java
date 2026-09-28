package org.streaming.dto;

import org.streaming.entity.TelemetryPointEntity;

public record TelemetryPointResponseDto(
        Float x,
        Float y,
        String timestamp,
        Float speed
) {
    public static TelemetryPointResponseDto fromEntity(TelemetryPointEntity telemetryPointEntity) {
        return new TelemetryPointResponseDto(
                telemetryPointEntity.getX(),
                telemetryPointEntity.getY(),
                telemetryPointEntity.getTimestamp(),
                telemetryPointEntity.getSpeed()
        );
    }
}
