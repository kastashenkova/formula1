package org.example.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public record BatchEntity(
        UUID batch_id,
        String raceName,
        Integer year,
        LocalDateTime createdAt,
        LocalDateTime deletedAt
) {
}
