package org.identity.service;


import org.identity.dto.WebhookRequestDto;
import org.identity.dto.WebhookResponseDto;

public interface WebhookService {

    WebhookResponseDto create(WebhookRequestDto request);

    WebhookResponseDto getById(Long id);

    WebhookResponseDto update(Long id, WebhookRequestDto request);

    void delete(Long id);
}
