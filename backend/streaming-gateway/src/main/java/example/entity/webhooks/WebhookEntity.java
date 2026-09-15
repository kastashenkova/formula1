package example.entity.webhooks;

import java.time.LocalDateTime;

public record WebhookEntity(
        Long id,
        String webhookURL,
        Long userID,
        String webhookType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
