package org.example.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import org.example.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class RaceImportedWebhookIntegrationTest {

    private static final String ADMIN_EMAIL = "webhook.admin@ukma.edu.ua";
    private static final String WEBHOOK_PATH = "/hooks/race-imported";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private WireMockServer openF1Server;

    @BeforeEach
    void setUp() {
        openF1Server.resetRequests();
        openF1Server.stubFor(WireMock.post(WireMock.urlPathEqualTo(WEBHOOK_PATH))
                .willReturn(WireMock.aResponse().withStatus(200)));

        UUID adminId = UUID.randomUUID();
        jdbc.sql("INSERT INTO users (id, email, phone_number, password, role, user_status) VALUES (?, ?, ?, ?, ?, ?)")
                .params(adminId, ADMIN_EMAIL, "+380980137098", "password", "ADMIN", "ACTIVE")
                .update();
        insertWebhook(1L, adminId, "RACE_IMPORTED_SUCCESS");
        insertWebhook(2L, adminId, "RACE_IMPORTED_FAILED");
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void createRace_ShouldSendOnlySuccessWebhookToCreator() throws Exception {
        mockMvc.perform(post("/api/races").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "Monaco",
                                    "year": 2024
                                }
                                """))
                .andExpect(status().isCreated());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                openF1Server.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(WEBHOOK_PATH))
                        .withRequestBody(WireMock.matchingJsonPath("$.type", WireMock.equalTo("RACE_IMPORTED_SUCCESS")))
                        .withRequestBody(WireMock.matchingJsonPath("$.race_name", WireMock.equalTo("Monaco")))));
        openF1Server.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(WEBHOOK_PATH)));
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void createRaceWhenImportFails_ShouldSendOnlyFailedWebhook() throws Exception {
        mockMvc.perform(post("/api/races").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "Imola",
                                    "year": 2024
                                }
                                """))
                .andExpect(status().isBadGateway());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                openF1Server.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(WEBHOOK_PATH))
                        .withRequestBody(WireMock.matchingJsonPath("$.type", WireMock.equalTo("RACE_IMPORTED_FAILED")))
                        .withRequestBody(WireMock.matchingJsonPath("$.race_name", WireMock.equalTo("Imola")))
                        .withRequestBody(WireMock.matchingJsonPath("$.error"))));
        openF1Server.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(WEBHOOK_PATH)));
    }

    private void insertWebhook(Long id, UUID userId, String type) {
        jdbc.sql("INSERT INTO webhooks (id, webhook_url, user_id, webhook_type, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)")
                .params(id, openF1Server.baseUrl() + WEBHOOK_PATH, userId, type, LocalDateTime.now(), LocalDateTime.now())
                .update();
    }
}
