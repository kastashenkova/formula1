package example.dto.webhooks;

import com.fasterxml.jackson.annotation.JsonProperty;
import example.enums.WebhookTypes;

import java.time.LocalDateTime;

public record ResponseWebhookDTO(
        Long id,

        @JsonProperty("webhook_url")
        String webhookURL,

        @JsonProperty("user_id")
        Long userID,

        @JsonProperty("webhook_type")
        WebhookTypes webhookType,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("updated_t")
        LocalDateTime updatedAt
) {

}
