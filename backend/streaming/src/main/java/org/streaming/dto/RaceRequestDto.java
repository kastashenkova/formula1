package org.streaming.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import org.streaming.entity.RaceEntity;
import org.streaming.validation.OnCreate;
import org.streaming.validation.OnUpdate;

@Schema(description = "Create new race request")
public record RaceRequestDto(
        @Null(groups = OnCreate.class, message = "{validation.raceId.creation.null}")
        @NotNull(groups = OnUpdate.class, message = "{validation.raceId.update.not-null}")
        UUID raceId,
        @Schema(description = "Race name", example = "Singapore Airlines Singapore GP")
        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.raceName.not-blank}")
        @Size(min = 3, max = 100, groups = {OnCreate.class, OnUpdate.class})
        String raceName,
        @Schema(description = "Race date", example = "2026-10-11T15:00:00.000Z")
        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.raceDate.not-blank}")
        LocalDateTime raceDate
) {
    public static RaceEntity toEntity(RaceRequestDto requestDto) {
        return new RaceEntity(requestDto.raceName, requestDto.raceDate);
    }
}