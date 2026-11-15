package org.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;
import org.identity.enums.WebhookTypes;
import org.streaming.dto.RaceImportedEvent;

public record RaceImportedWebhookDto(
        @JsonProperty("type")
        String type,
        @JsonProperty("race_id")
        UUID raceId,
        @JsonProperty("race_name")
        String raceName,
        @JsonProperty("race_date")
        LocalDateTime raceDate
) {
    public static RaceImportedWebhookDto fromEvent(RaceImportedEvent event) {
        return new RaceImportedWebhookDto(
                WebhookTypes.RACE_IMPORTED.name(),
                event.raceId(),
                event.raceName(),
                event.raceDate()
        );
    }
}
