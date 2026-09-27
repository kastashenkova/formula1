package org.example.entity;

import java.time.LocalDateTime;

public record WebhookEntity(
        Long id,
        String webhookURL,
        Long userID, // should be 'User user' here when db exists
        String webhookType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
