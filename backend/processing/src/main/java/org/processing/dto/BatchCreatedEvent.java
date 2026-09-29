package org.processing.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BatchCreatedEvent(
        UUID batchId,
        LocalDateTime createdAt
) {
}
