package org.identity.service.internal;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import org.identity.dto.WebhookRequestDto;
import org.identity.dto.WebhookResponseDto;
import org.identity.entity.UserEntity;
import org.identity.entity.WebhookEntity;
import org.identity.repository.UserRepository;
import org.identity.repository.WebhookRepository;
import org.identity.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WebhookServiceImpl implements WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookServiceImpl.class);

    private final WebhookRepository webhookRepository;
    private final UserRepository userRepository;

    public WebhookServiceImpl(WebhookRepository webhookRepository, UserRepository userRepository) {
        this.webhookRepository = webhookRepository;
        this.userRepository = userRepository;
    }

    @Override
    public WebhookResponseDto create(WebhookRequestDto request) {
        UserEntity user = userRepository.findById(request.userID())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "User with id " + request.userID() + " not found"
                        ));

        WebhookEntity webhook = WebhookRequestDto.toEntity(request);
        webhook.setUser(user);

        WebhookEntity saved = webhookRepository.save(webhook);

        log.info("Created webhook {}", saved.getId());

        return WebhookResponseDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WebhookResponseDto getById(Long id) {
        WebhookEntity webhook = webhookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Webhook with id "
                        + id + " not found"));

        return WebhookResponseDto.fromEntity(webhook);
    }

    @Override
    public WebhookResponseDto update(Long id, WebhookRequestDto request) {
        WebhookEntity webhookEntity = webhookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Webhook with id: " + id + " not found"));

        webhookEntity.setWebhookUrl(request.webhookURL());
        webhookEntity.setWebhookType(request.webhookType());
        webhookEntity.setUpdatedAt(LocalDateTime.now());

        webhookRepository.save(webhookEntity);

        log.info("Updated webhook {}", webhookEntity.getId());

        return WebhookResponseDto.fromEntity(webhookEntity);
    }

    @Override
    public void delete(Long id) {
        webhookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Webhook with id: " + id + " not found"));

        webhookRepository.deleteById(id);
    }
}
