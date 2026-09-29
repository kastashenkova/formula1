package org.identity.service.internal;

import jakarta.transaction.Transactional;
import org.identity.dto.WebhookRequestDto;
import org.identity.dto.WebhookResponseDto;
import org.identity.entity.WebhookEntity;
import org.identity.enums.WebhookTypes;

import org.identity.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WebhookServiceImpl implements WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookServiceImpl.class);

    @Override
    @Transactional
    public WebhookResponseDto create(WebhookRequestDto request) {
        return null;
    }

    @Override
    public WebhookResponseDto getById(Long id) {
        return null;
    }

    @Override
    @Transactional
    public WebhookResponseDto update(Long id, WebhookRequestDto request) {
        return null;
    }

    @Override
    @Transactional
    public void delete(Long id) {
    }

    private WebhookResponseDto toResponseDto(WebhookEntity webhook) {
        return new WebhookResponseDto(
                webhook.getId(),
                webhook.getWebhookUrl(),
                webhook.getId(), // TODO user id here
                WebhookTypes.valueOf(webhook.getWebhookType()),
                webhook.getCreatedAt(),
                webhook.getUpdatedAt()
        );
    }
}
