package org.identity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;
import org.identity.entity.WebhookEntity;

public record WebhookResponseDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,

        @JsonProperty("webhook_url")
        String webhookURL,

        @JsonProperty("user_id")
        UUID userID,

        @JsonProperty("webhook_type")
        String webhookType,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {

        public static WebhookResponseDto fromEntity(WebhookEntity webhook) {
                return new WebhookResponseDto(
                        webhook.getId(),
                        webhook.getWebhookUrl(),
                        webhook.getUser().getId(),
                        webhook.getWebhookType(),
                        webhook.getCreatedAt(),
                        webhook.getUpdatedAt()
                );
        }
}
