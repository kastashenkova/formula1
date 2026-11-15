package org.streaming.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RaceImportedEvent(
        UUID raceId,
        String raceName,
        LocalDateTime raceDate,
        String requestedBy
) {
}
