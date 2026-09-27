package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.validation.OnCreate;
import org.example.validation.OnUpdate;

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
}
