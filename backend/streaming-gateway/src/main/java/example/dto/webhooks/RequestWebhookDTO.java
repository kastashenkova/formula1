package example.dto.webhooks;

import example.enums.WebhookTypes;
import example.validation.OnCreate;
import example.validation.OnUpdate;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

public record RequestWebhookDTO(
        @Null(
                groups = {OnCreate.class, OnUpdate.class},
                message = "{validation.webhook.id.null}"
        )
        Long id,

        @NotNull(message = "{validation.webhookURL.not-null}")
        @URL(message = "{validation.webhookURL.url}")
        @Size(
                min = 5,
                max = 500,
                message = "{validation.webhookURL.size}"
        )
        String webhookURL,

        @NotNull(message = "{validation.userID.not-null}")
        @Min(
                value = 1,
                message = "{validation.userID.min}"
        )
        Long userID,

        @NotNull(message = "{validation.webhookType.not-null}")
        WebhookTypes webhookType
) {
}
