package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import org.example.enums.WebhookTypes;

public record WebhookResponseDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,

        @JsonProperty("webhook_url")
        String webhookURL,

        @JsonProperty("user_id")
        Long userID,

        @JsonProperty("webhook_type")
        WebhookTypes webhookType,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {

}
