package org.streaming.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.streaming.validation.OnCreate;

@Schema(description = "Create new race request")
public record RaceRequestDto(
        @Null(groups = OnCreate.class, message = "{validation.raceId.creation.null}")
        UUID raceId,
        @Schema(description = "Race country name", example = "Monaco")
        @NotBlank(groups = OnCreate.class, message = "{validation.raceName.not-blank}")
        @Size(min = 3, max = 100, groups = OnCreate.class, message = "{validation.raceName.size}")
        String raceName,
        @Schema(description = "Race year", example = "2024")
        @NotNull(groups = OnCreate.class, message = "{validation.year.not-null}")
        @Min(value = 2023, groups = OnCreate.class, message = "{validation.year.min}")
        Integer year
) {
}
