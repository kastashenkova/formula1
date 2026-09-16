package org.example.service.webhook;

import org.example.dto.webhooks.WebhookRequestDto;
import org.example.dto.webhooks.WebhookResponseDto;
import org.example.entity.webhooks.WebhookEntity;
import org.example.enums.WebhookTypes;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class WebhookServiceImpl implements WebhookService {

    private final Map<Long, WebhookEntity> storage = new ConcurrentHashMap<>();

    @Override
    public WebhookResponseDto create(WebhookRequestDto request) {
        long id = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        var now = LocalDateTime.now();

        var entity = new WebhookEntity(
                id,
                request.webhookURL(),
                request.userID(),
                request.webhookType().toString(),
                now,
                now
        );

        storage.put(id, entity);

        return toResponseDto(entity);
    }

    @Override
    public WebhookResponseDto getById(Long id) {
        var webhook = storage.get(id);

        if (webhook == null) {
            throw new EntityNotFoundException(
                    "Webhook not found with id: " + id
            );
        }

        return toResponseDto(webhook);
    }

    @Override
    public WebhookResponseDto update(Long id, WebhookRequestDto request) {
        var existing = storage.get(id);

        if (existing == null) {
            throw new EntityNotFoundException(
                    "Webhook not found with id: " + id
            );
        }

        var updated = new WebhookEntity(
                existing.id(),
                request.webhookURL(),
                request.userID(),
                request.webhookType().toString(),
                existing.createdAt(),
                LocalDateTime.now()
        );

        storage.put(id, updated);

        return toResponseDto(updated);
    }

    @Override
    public void delete(Long id) {
        var removed = storage.remove(id);

        if (removed == null) {
            throw new EntityNotFoundException(
                    "Webhook not found with id: " + id
            );
        }
    }

    private WebhookResponseDto toResponseDto(WebhookEntity webhook) {
        return new WebhookResponseDto(
                webhook.id(),
                webhook.webhookURL(),
                webhook.userID(),
                WebhookTypes.valueOf(webhook.webhookType()),
                webhook.createdAt(),
                webhook.updatedAt()
        );
    }
}