package example.service.streaming.webhooks;

import example.dto.webhooks.RequestWebhookDTO;
import example.dto.webhooks.ResponseWebhookDTO;
import org.springframework.stereotype.Service;

@Service
public interface WebhookService {

    ResponseWebhookDTO create(RequestWebhookDTO request);

    ResponseWebhookDTO getById(Long id);

    ResponseWebhookDTO update(Long id, RequestWebhookDTO request);

    void delete(Long id);
}
