package org.example.service.webhook;

import org.example.dto.webhooks.WebhookRequestDto;
import org.example.dto.webhooks.WebhookResponseDto;
import org.springframework.stereotype.Service;

public interface WebhookService {

    WebhookResponseDto create(WebhookRequestDto request);

    WebhookResponseDto getById(Long id);

    WebhookResponseDto update(Long id, WebhookRequestDto request);

    void delete(Long id);
}
