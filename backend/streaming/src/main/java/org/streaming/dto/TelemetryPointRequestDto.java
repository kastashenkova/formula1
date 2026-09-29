package org.streaming.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.streaming.validation.OnCreate;
import org.streaming.validation.OnUpdate;

public record TelemetryPointRequestDto(
        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.pointX.not-null}")
        Integer x,

        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.pointY.not-null}")
        Integer y,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.timestamp.not-blank}")
        String timestamp
) {
}
