package example.service.streaming.webhooks;

import example.dto.webhooks.RequestWebhookDTO;
import example.dto.webhooks.ResponseWebhookDTO;
import example.entity.webhooks.WebhookEntity;
import example.enums.WebhookTypes;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class WebhookServiceImpl implements WebhookService {

    private final Map<Long, WebhookEntity> storage = new ConcurrentHashMap<>();

    @Override
    public ResponseWebhookDTO create(RequestWebhookDTO request) {
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
    public ResponseWebhookDTO getById(Long id) {
        var webhook = storage.get(id);

        if (webhook == null) {
            throw new EntityNotFoundException(
                    "Webhook not found with id: " + id
            );
        }

        return toResponseDto(webhook);
    }

    @Override
    public ResponseWebhookDTO update(Long id, RequestWebhookDTO request) {
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

    private ResponseWebhookDTO toResponseDto(WebhookEntity webhook) {
        return new ResponseWebhookDTO(
                webhook.id(),
                webhook.webhookURL(),
                webhook.userID(),
                WebhookTypes.valueOf(webhook.webhookType()),
                webhook.createdAt(),
                webhook.updatedAt()
        );
    }
}