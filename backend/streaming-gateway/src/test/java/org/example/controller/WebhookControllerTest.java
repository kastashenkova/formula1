package org.example.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import org.example.dto.webhooks.WebhookRequestDto;
import org.example.dto.webhooks.WebhookResponseDto;
import org.example.enums.WebhookTypes;
import org.example.service.webhook.WebhookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WebhookController.class)
public class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebhookService webhookService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    @DisplayName("Should add new Webhook")
    void createWebhook_relevantData_Success() throws Exception {
        WebhookRequestDto newWebhookRequestDto = new WebhookRequestDto(
                null,
                "https://example-url.com",
                1L,
                WebhookTypes.BATCH_COMPLETED
        );

        WebhookResponseDto newWebhookResponseDto = new WebhookResponseDto(
                1L,
                newWebhookRequestDto.webhookURL(),
                newWebhookRequestDto.userID(),
                newWebhookRequestDto.webhookType(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(webhookService.create(newWebhookRequestDto))
                .thenReturn(newWebhookResponseDto);

        mockMvc.perform(post("/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newWebhookRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(webhookService, times(1))
                .create(newWebhookRequestDto);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when webhook URL is invalid")
    void createWebhook_invalidURL_ReturnsBadRequest() throws Exception {
        WebhookRequestDto invalidRequest = new WebhookRequestDto(
                null,
                "invalidURL",
                1L,
                WebhookTypes.BATCH_COMPLETED
        );

        mockMvc.perform(post("/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(webhookService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when user id is 0")
    void createWebhook_invalidUserId_ReturnsBadRequest() throws Exception {
        WebhookRequestDto invalidRequest = new WebhookRequestDto(
                null,
                "https://example-url.com",
                0L,
                WebhookTypes.BATCH_COMPLETED
        );

        mockMvc.perform(post("/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(webhookService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when webhook type is undefined")
    void createWebhook_invalidWebhookType_ReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                    "webhookURL": "https://example-url.com",
                    "userID": 1,
                    "webhookType": "INVALID_WEBHOOK_TYPE_HERE"
                }
                """;

        mockMvc.perform(post("/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(webhookService);
    }

    @Test
    @DisplayName("Should return 404 Not Found when webhook does not exist")
    void getWebhook_notExists_ReturnsNotFound() throws Exception {
        when(webhookService.getById(1L)).thenThrow(new EntityNotFoundException("Not found"));

        mockMvc.perform(get("/webhooks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(webhookService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("Should return a relevant Webhook")
    void getWebhook_existingEntity_Success() throws Exception {
        WebhookRequestDto newWebhookRequestDto = new WebhookRequestDto(
                null,
                "https://example-url.com",
                1L,
                WebhookTypes.BATCH_COMPLETED
        );

        WebhookResponseDto newWebhookResponseDto = new WebhookResponseDto(
                1L,
                newWebhookRequestDto.webhookURL(),
                newWebhookRequestDto.userID(),
                newWebhookRequestDto.webhookType(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(webhookService.getById(1L)).thenReturn(newWebhookResponseDto);

        mockMvc.perform(get("/webhooks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));

        verify(webhookService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("Should return 404 Not Found when webhook does not exist")
    void deleteWebhook_notExists_ReturnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Not found")).when(webhookService).delete(1L);

        mockMvc.perform(delete("/webhooks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(webhookService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Should delete a webhook successfully")
    void deleteWebhook_existingEntity_Success() throws Exception {
        doNothing().when(webhookService).delete(1L);

        mockMvc.perform(delete("/webhooks/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(webhookService, times(1))
                .delete(1L);
    }

    @Test
    @DisplayName("Should return a relevant Webhook after updating it")
    void updateWebhook_existingEntity_Success() throws Exception {
        WebhookRequestDto updateRequestDto = new WebhookRequestDto(
                null,
                "https://example-url.com",
                1L,
                WebhookTypes.BATCH_COMPLETED
        );

        WebhookResponseDto newWebhookResponseDto = new WebhookResponseDto(
                1L,
                updateRequestDto.webhookURL(),
                updateRequestDto.userID(),
                updateRequestDto.webhookType(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(webhookService.update(1L, updateRequestDto)).thenReturn(newWebhookResponseDto);

        mockMvc.perform(put("/webhooks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(webhookService, times(1))
                .update(1L, updateRequestDto);
    }

    @Test
    @DisplayName("Should return 404 Not Found when webhook does not exist")
    void updateWebhook_notExists_ReturnsNotFound() throws Exception {
        WebhookRequestDto updateRequestDto = new WebhookRequestDto(
                null,
                "https://example-url.com",
                1L,
                WebhookTypes.BATCH_COMPLETED
        );

        when(webhookService.update(eq(1L), any(WebhookRequestDto.class)))
                .thenThrow(new EntityNotFoundException("Not found"));

        mockMvc.perform(put("/webhooks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isNotFound());

        verify(webhookService, times(1)).update(eq(1L), any(WebhookRequestDto.class));
    }
}
