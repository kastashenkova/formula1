package org.identity.service;

import jakarta.validation.Valid;
import org.identity.dto.WebhookRequestDto;
import org.identity.dto.WebhookResponseDto;
import org.identity.validation.OnCreate;
import org.identity.validation.OnUpdate;
import org.springframework.validation.annotation.Validated;

@Validated
public interface WebhookService {

    @Validated(OnCreate.class)
    WebhookResponseDto create(@Valid WebhookRequestDto request);

    WebhookResponseDto getById(Long id);

    @Validated(OnUpdate.class)
    WebhookResponseDto update(Long id, @Valid WebhookRequestDto request);

    void delete(Long id);
}
