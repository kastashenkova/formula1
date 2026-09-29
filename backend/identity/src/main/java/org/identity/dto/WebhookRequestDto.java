package org.identity.dto;

import org.identity.entity.WebhookEntity;
import org.identity.validation.OnCreate;
import org.identity.validation.OnUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
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
        UUID userID,

        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.webhookType.not-null}")
        String webhookType
) {

        public static WebhookEntity toEntity(WebhookRequestDto requestDto) {
                return new WebhookEntity(
                        requestDto.webhookURL,
                        requestDto.webhookType,
                        LocalDateTime.now(),
                        LocalDateTime.now());
        }
}
