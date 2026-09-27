package org.streaming.service;

import org.streaming.dto.WebhookRequestDto;
import org.streaming.dto.WebhookResponseDto;

public interface WebhookService {

    WebhookResponseDto create(WebhookRequestDto request);

    WebhookResponseDto getById(Long id);

    WebhookResponseDto update(Long id, WebhookRequestDto request);

    void delete(Long id);
}
