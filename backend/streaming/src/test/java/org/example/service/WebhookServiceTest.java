package org.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.EntityNotFoundException;
import org.example.dto.WebhookRequestDto;
import org.example.dto.WebhookResponseDto;
import org.example.enums.WebhookTypes;
import org.example.service.internal.WebhookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WebhookServiceTest {

    private WebhookServiceImpl webhookService;

    @BeforeEach
    void setUp() {
        webhookService = new WebhookServiceImpl();
    }

    @Test
    void shouldCreateWebhookSuccessfully() {
        WebhookRequestDto requestDto = new WebhookRequestDto(
                null,
                "https://example-url.com",
                1L,
                WebhookTypes.BATCH_COMPLETED
        );

        WebhookResponseDto responseDto = webhookService.create(requestDto);

        assertNotNull(responseDto);
        assertNotNull(responseDto.id());
        assertEquals(requestDto.webhookURL(), responseDto.webhookURL());
        assertEquals(requestDto.userID(), responseDto.userID());
        assertEquals(requestDto.webhookType(), responseDto.webhookType());
        assertNotNull(responseDto.createdAt());
        assertNotNull(responseDto.updatedAt());
        assertEquals(responseDto.createdAt(), responseDto.updatedAt());
    }

    @Test
    void shouldReturnWebhookByIdSuccessfully() {
        WebhookRequestDto requestDto = new WebhookRequestDto(
                null,
                "https://example-url.com",
                1L,
                WebhookTypes.BATCH_COMPLETED
        );

        WebhookResponseDto created = webhookService.create(requestDto);

        WebhookResponseDto found = webhookService.getById(created.id());

        assertNotNull(found);
        assertEquals(created.id(), found.id());
        assertEquals(created.webhookURL(), found.webhookURL());
        assertEquals(created.userID(), found.userID());
        assertEquals(created.webhookType(), found.webhookType());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingNonExistentWebhook() {
        assertThrows(EntityNotFoundException.class,
                () -> webhookService.getById(999L));
    }

    @Test
    void shouldUpdateWebhookSuccessfully() {
        WebhookRequestDto createDto = new WebhookRequestDto(
                null,
                "https://example-url.com",
                1L,
                WebhookTypes.BATCH_COMPLETED
        );
        WebhookResponseDto created = webhookService.create(createDto);

        WebhookRequestDto updateDto = new WebhookRequestDto(
                null,
                "https://updated-url.com",
                2L,
                WebhookTypes.BATCH_COMPLETED
        );

        WebhookResponseDto updated = webhookService.update(created.id(), updateDto);

        assertNotNull(updated);
        assertEquals(created.id(), updated.id());
        assertEquals(updateDto.webhookURL(), updated.webhookURL());
        assertEquals(updateDto.userID(), updated.userID());
        assertEquals(created.createdAt(), updated.createdAt());
        assertNotNull(updated.updatedAt());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUpdatingNonExistentWebhook() {
        WebhookRequestDto updateDto = new WebhookRequestDto(
                null,
                "https://updated-url.com",
                2L,
                WebhookTypes.BATCH_COMPLETED
        );

        assertThrows(EntityNotFoundException.class,
                () -> webhookService.update(999L, updateDto));
    }

    @Test
    void shouldDeleteWebhookSuccessfully() {
        WebhookRequestDto createDto = new WebhookRequestDto(
                null,
                "https://example-url.com",
                1L,
                WebhookTypes.BATCH_COMPLETED
        );
        WebhookResponseDto created = webhookService.create(createDto);

        webhookService.delete(created.id());

        assertThrows(EntityNotFoundException.class,
                () -> webhookService.getById(created.id()));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeletingNonExistentWebhook() {
        assertThrows(EntityNotFoundException.class,
                () -> webhookService.delete(999L));
    }
}
