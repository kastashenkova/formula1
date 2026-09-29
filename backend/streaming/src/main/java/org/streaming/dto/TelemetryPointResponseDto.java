package org.streaming.dto;

import java.time.LocalDateTime;
import org.streaming.entity.TelemetryPointEntity;

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
