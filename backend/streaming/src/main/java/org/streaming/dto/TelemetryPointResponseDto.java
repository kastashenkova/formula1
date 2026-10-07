package org.streaming.dto;

import org.streaming.entity.TelemetryPointEntity;

import java.time.LocalDateTime;

public record TelemetryPointResponseDto(
        Integer x,
        Integer y,
        LocalDateTime timestamp
) {
    public static TelemetryPointResponseDto fromEntity(TelemetryPointEntity telemetryPointEntity) {
        return new TelemetryPointResponseDto(
                telemetryPointEntity.getX(),
                telemetryPointEntity.getY(),
                telemetryPointEntity.getTimestamp()
        );
    }
}
