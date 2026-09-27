package org.streaming.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import org.streaming.entity.RaceEntity;
import org.streaming.validation.OnCreate;
import org.streaming.validation.OnUpdate;

public record RaceRequestDto(
        @Null(groups = OnCreate.class, message = "{validation.raceName.creation.null}")
        @NotNull(groups = OnUpdate.class, message = "{validation.raceName.update.not-null}")
        UUID raceId,
        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.raceName.not-blank}")
        @Size(min = 3, max = 100, groups = {OnCreate.class, OnUpdate.class})
        String raceName,
        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.raceDate.not-blank}")
        LocalDateTime raceDate
) {
    public static RaceEntity toEntity(RaceRequestDto requestDto) {
        return new RaceEntity(requestDto.raceId, requestDto.raceName, requestDto.raceDate);
    }
}