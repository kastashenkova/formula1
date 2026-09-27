package org.example.dto;

import org.example.validation.OnCreate;
import org.example.validation.OnUpdate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record BatchRequestDto(
        @Null(groups = OnCreate.class, message = "{validation.batchId.creation.null}")
        @NotNull(groups = OnUpdate.class, message = "{validation.batchId.update.not-null}")
        UUID batchId,

        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.raceName.not-null}")
        @Size(groups = {OnCreate.class, OnUpdate.class}, min = 3, max = 100, message = "{validation.raceName.size}")
        String raceName,

        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.batchYear.not-null}")
        @Min(groups = {OnCreate.class, OnUpdate.class}, value = 1950, message = "{validation.batchYear.min}")
        @Max(groups = {OnCreate.class, OnUpdate.class}, value = 2026, message = "{validation.batchYear.max}")
        Integer year,
        LocalDateTime createdAt,
        LocalDateTime deletedAt
) {
}
