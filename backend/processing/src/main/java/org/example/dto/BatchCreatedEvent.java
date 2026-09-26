package org.example.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record BatchCreatedEvent(
        UUID batchId,
        String raceName,
        Integer year,
        LocalDateTime createdAt,
        LocalDateTime deletedAt
) {
}
