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
        LocalDateTime raceDate,
        @JsonProperty("error")
        String error
) {
    public static RaceImportedWebhookDto fromEvent(RaceImportedEvent event, WebhookTypes type) {
        return new RaceImportedWebhookDto(
                type.name(),
                event.raceId(),
                event.raceName(),
                event.raceDate(),
                event.error()
        );
    }
}
