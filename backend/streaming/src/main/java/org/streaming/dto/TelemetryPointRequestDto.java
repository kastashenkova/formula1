package org.streaming.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import org.streaming.entity.TelemetryPointEntity;
import org.streaming.validation.OnCreate;
import org.streaming.validation.OnUpdate;

@Schema(description = "Create new telemetry point request")
public record TelemetryPointRequestDto(
        @Schema(description = "Point X of driver's location", example = "10")
        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.pointX.not-null}")
        Integer x,

        @Schema(description = "Point Y of driver's location", example = "22")
        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.pointY.not-null}")
        Integer y,

        @Schema(description = "Timestamp of the Point", example = "2026-10-11T15:22:00.000Z")
        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.timestamp.not-blank}")
        LocalDateTime timestamp
) {
        public static TelemetryPointEntity toEntity(TelemetryPointRequestDto requestDto) {
                return new TelemetryPointEntity (
                        requestDto.x,
                        requestDto.y,
                        requestDto.timestamp
                );
        }
}
