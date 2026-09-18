package org.example.event;

import org.example.enums.BatchStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record BatchCreatedEvent(
        UUID batchId,
        String raceName,
        Integer year,
        LocalDateTime createdAt,
        LocalDateTime deletedAt,
        BatchStatus batchStatus
) {
}
