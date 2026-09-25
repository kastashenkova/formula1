package org.example.service;

import org.example.dto.WebhookRequestDto;
import org.example.dto.WebhookResponseDto;

public interface WebhookService {

    WebhookResponseDto create(WebhookRequestDto request);

    WebhookResponseDto getById(Long id);

    WebhookResponseDto update(Long id, WebhookRequestDto request);

    void delete(Long id);
}
