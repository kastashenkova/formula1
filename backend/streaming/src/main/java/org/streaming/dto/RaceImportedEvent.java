package org.streaming.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RaceImportedEvent(
        UUID raceId,
        String raceName,
        LocalDateTime raceDate,
        String requestedBy,
        Status status,
        String error
) {
    public enum Status {
        SUCCESS, FAILED
    }

    public static RaceImportedEvent success(UUID raceId, String raceName, LocalDateTime raceDate, String requestedBy) {
        return new RaceImportedEvent(raceId, raceName, raceDate, requestedBy, Status.SUCCESS, null);
    }

    public static RaceImportedEvent failed(UUID raceId, String raceName, LocalDateTime raceDate, String requestedBy,
                                           String error) {
        return new RaceImportedEvent(raceId, raceName, raceDate, requestedBy, Status.FAILED, error);
    }
}
