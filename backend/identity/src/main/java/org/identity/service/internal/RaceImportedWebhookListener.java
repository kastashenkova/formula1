package org.identity.service.internal;

import java.util.List;
import org.identity.dto.RaceImportedWebhookDto;
import org.identity.entity.WebhookEntity;
import org.identity.enums.WebhookTypes;
import org.identity.repository.WebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.streaming.dto.RaceImportedEvent;

@Component
public class RaceImportedWebhookListener {
    private static final Logger log = LoggerFactory.getLogger(RaceImportedWebhookListener.class);
    private final WebhookRepository webhookRepository;
    private final RestClient restClient;

    RaceImportedWebhookListener(WebhookRepository webhookRepository, RestClient.Builder restClientBuilder) {
        this.webhookRepository = webhookRepository;
        this.restClient = restClientBuilder.build();
    }

    @ApplicationModuleListener
    void sendRaceImportedWebhooks(RaceImportedEvent event) {
        WebhookTypes type = switch (event.status()) {
            case SUCCESS -> WebhookTypes.RACE_IMPORTED_SUCCESS;
            case FAILED -> WebhookTypes.RACE_IMPORTED_FAILED;
        };
        List<WebhookEntity> webhooks = webhookRepository.findAllByUserEmailAndWebhookType(
                event.requestedBy(), type.name());
        RaceImportedWebhookDto payload = RaceImportedWebhookDto.fromEvent(event, type);
        int failed = 0;

        for (WebhookEntity webhook : webhooks) {
            try {
                restClient.post()
                        .uri(webhook.getWebhookUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();
                log.info("Sent race {} imported webhook {}", event.raceId(), webhook.getId());
            } catch (RestClientException e) {
                failed++;
                log.warn("Failed to send race {} imported webhook {}", event.raceId(), webhook.getId(), e);
            }
        }

        if (failed > 0) {
            // Keeps the event publication incomplete, so it is resent on restart
            throw new IllegalStateException(String.format("%d of %d webhooks for race %s failed",
                    failed, webhooks.size(), event.raceId()));
        }
    }
}
