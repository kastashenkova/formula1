package org.example.dto.webhooks;

import org.example.enums.WebhookTypes;
import org.example.validation.OnCreate;
import org.example.validation.OnUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record WebhookRequestDto(
        @Null(
                groups = {OnCreate.class, OnUpdate.class},
                message = "{validation.webhook.id.null}"
        )
        Long id,

        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.webhookURL.not-null}")
        @URL(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.webhookURL.url}")
        @Size(groups = {OnCreate.class, OnUpdate.class},
                min = 5,
                max = 500,
                message = "{validation.webhookURL.size}"
        )
        String webhookURL,

        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.userID.not-null}")
        @Min(groups = {OnCreate.class, OnUpdate.class},
                value = 1,
                message = "{validation.userID.min}"
        )
        Long userID,

        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.webhookType.not-null}")
        WebhookTypes webhookType
) {
}
