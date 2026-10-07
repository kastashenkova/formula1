package org.streaming.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.streaming.entity.RaceEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public record RaceResponseDto(
        @JsonProperty("race_id")
        UUID raceId,
        @JsonProperty("race_name")
        String raceName,
        @JsonProperty("race_date")
        LocalDateTime raceDate
) {
    public static RaceResponseDto fromEntity(RaceEntity raceEntity) {
        return new RaceResponseDto(
                raceEntity.getRaceId(),
                raceEntity.getRaceName(),
                raceEntity.getRaceDate()
        );
    }
}
