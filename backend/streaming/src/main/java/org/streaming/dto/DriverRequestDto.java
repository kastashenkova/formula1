package org.streaming.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.streaming.entity.DriverEntity;
import org.streaming.validation.OnCreate;
import org.streaming.validation.OnUpdate;

@Schema(description = "Create new driver request")
public record DriverRequestDto(
        @Schema(description = "Driver number", example = "3")
        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.driverNumber.not-null}")
        @Min(groups = {OnCreate.class, OnUpdate.class}, value = 2, message = "{validation.driverNumber.min}")
        @Max(groups = {OnCreate.class, OnUpdate.class}, value = 99, message = "{validation.driverNumber.max}")
        Long driverNumber,

        @Schema(description = "Full name", example = "Max Verstappen")
        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.driverFullName.not-blank}")
        String fullName
) {
        public static DriverEntity toEntity(DriverRequestDto requestDto) {
                return new DriverEntity(
                        requestDto.driverNumber,
                        requestDto.fullName);
        }
}
