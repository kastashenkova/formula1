package org.streaming.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.streaming.entity.DriverEntity;
import org.streaming.entity.RaceEntity;
import org.streaming.validation.OnCreate;
import org.streaming.validation.OnUpdate;

import java.util.UUID;

public record DriverRequestDto(
        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.driverNumber.not-null}")
        @Min(groups = {OnCreate.class, OnUpdate.class}, value = 2, message = "{validation.driverNumber.min}")
        @Max(groups = {OnCreate.class, OnUpdate.class}, value = 99, message = "{validation.driverNumber.max}")
        Long driverNumber,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.driverFullName.not-blank}")
        String fullName
) {
        public static DriverEntity toEntity(DriverRequestDto requestDto) {
                return new DriverEntity(
                        requestDto.driverNumber,
                        requestDto.fullName);
        }
}
