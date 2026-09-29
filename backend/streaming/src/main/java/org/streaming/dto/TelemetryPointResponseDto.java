package org.streaming.dto;

import org.streaming.entity.TelemetryPointEntity;

public record TelemetryPointResponseDto(
        Integer x,
        Integer y,
        String timestamp
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
