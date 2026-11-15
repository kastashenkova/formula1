package org.identity.service.internal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.identity.entity.WebhookEntity;
import org.identity.repository.WebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.streaming.dto.RaceImportedEvent;

@ExtendWith(MockitoExtension.class)
class RaceImportedWebhookListenerTest {
    private static final String EMAIL = "admin@ukma.edu.ua";
    private static final RaceImportedEvent EVENT = new RaceImportedEvent(
            UUID.fromString("3f2a0d5e-7c1b-4b8e-9a51-2d6f0c8e4b17"),
            "Monaco",
            LocalDateTime.of(2024, 5, 26, 13, 0),
            EMAIL);

    @Mock
    private WebhookRepository webhookRepository;

    private MockRestServiceServer server;
    private RaceImportedWebhookListener listener;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        listener = new RaceImportedWebhookListener(webhookRepository, builder);
    }

    @Test
    void sendRaceImportedWebhooks_ShouldPostEventToEveryUserWebhook() {
        when(webhookRepository.findAllByUserEmailAndWebhookType(EMAIL, "RACE_IMPORTED"))
                .thenReturn(List.of(webhook(1L, "https://first.example/hook"), webhook(2L, "https://second.example/hook")));
        server.expect(requestTo("https://first.example/hook"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("RACE_IMPORTED"))
                .andExpect(jsonPath("$.race_id").value(EVENT.raceId().toString()))
                .andExpect(jsonPath("$.race_name").value("Monaco"))
                .andRespond(withSuccess());
        server.expect(requestTo("https://second.example/hook")).andRespond(withSuccess());

        listener.sendRaceImportedWebhooks(EVENT);

        server.verify();
    }

    @Test
    void sendRaceImportedWebhooks_ShouldDoNothing_WhenUserHasNoWebhooks() {
        when(webhookRepository.findAllByUserEmailAndWebhookType(EMAIL, "RACE_IMPORTED")).thenReturn(List.of());

        listener.sendRaceImportedWebhooks(EVENT);

        server.verify();
    }

    @Test
    void sendRaceImportedWebhooks_ShouldSendOthersAndFail_WhenOneWebhookFails() {
        when(webhookRepository.findAllByUserEmailAndWebhookType(EMAIL, "RACE_IMPORTED"))
                .thenReturn(List.of(webhook(1L, "https://broken.example/hook"), webhook(2L, "https://second.example/hook")));
        server.expect(requestTo("https://broken.example/hook")).andRespond(withServerError());
        server.expect(requestTo("https://second.example/hook")).andRespond(withSuccess());

        assertThrows(IllegalStateException.class, () -> listener.sendRaceImportedWebhooks(EVENT));

        server.verify();
    }

    private static WebhookEntity webhook(Long id, String url) {
        WebhookEntity webhook = new WebhookEntity(url, "RACE_IMPORTED", LocalDateTime.now(), LocalDateTime.now());
        webhook.setId(id);
        return webhook;
    }
}
