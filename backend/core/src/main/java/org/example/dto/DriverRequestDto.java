package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.validation.OnCreate;
import org.example.validation.OnUpdate;

public record DriverRequestDto(
        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.driverNumber.not-null}")
        @Min(groups = {OnCreate.class, OnUpdate.class}, value = 2, message = "{validation.driverNumber.min}")
        @Max(groups = {OnCreate.class, OnUpdate.class}, value = 99, message = "{validation.driverNumber.max}")
        Long driverNumber,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.driverCode.not-blank}")
        @Size(min = 3, max = 3, message = "{validation.driverCode.size}")
        String code,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.driverFullName.not-blank}")
        String fullName
) {
}
