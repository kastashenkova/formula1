package org.streaming.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.streaming.entity.TelemetryPointEntity;
import org.streaming.validation.OnCreate;
import org.streaming.validation.OnUpdate;

public record TelemetryPointRequestDto(
        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.pointX.not-null}")
        Float x,

        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.pointY.not-null}")
        Float y,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.timestamp.not-blank}")
        String timestamp,

        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.speed.not-null}")
        @Min(groups = {OnCreate.class, OnUpdate.class}, value = 0, message = "{validation.speed.min}")
        @Max(groups = {OnCreate.class, OnUpdate.class}, value = 400, message = "{validation.speed.max}")
        Float speed
) {
        public static TelemetryPointEntity toEntity(TelemetryPointRequestDto requestDto) {
                return new TelemetryPointEntity (
                        requestDto.x,
                        requestDto.y,
                        requestDto.timestamp,
                        requestDto.speed);
        }
}
