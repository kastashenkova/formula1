package org.example.integration;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class WebhookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        jdbc.sql("INSERT INTO users (id, email, phone_number, password, role, user_status) VALUES (?, ?, ?, ?, ?, ?)")
                .param(1, userId)
                .param(2, "astashenkova.katya@gmail.com")
                .param(3, "+380980137037")
                .param(4, "password")
                .param(5, "USER")
                .param(6, "ACTIVE")
                .update();

        jdbc.sql("""
                        INSERT INTO webhooks (id, webhook_url, user_id, webhook_type, created_at, updated_at)
                        VALUES (nextval('webhooks_seq'), ?, ?, ?, ?, ?)
                        """)
                .param(1, "https://example.com/hooks/pending")
                .param(2, userId)
                .param(3, "PENDING")
                .param(4, LocalDateTime.parse("2025-09-23T17:00:00"))
                .param(5, LocalDateTime.parse("2025-09-23T17:00:00"))
                .update();
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_ShouldStoreRow() throws Exception {
        mockMvc.perform(post("/api/webhooks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "webhookURL": "https://example.com/hooks/batch",
                                    "userID": "%s",
                                    "webhookType": "BATCH_COMPLETED"
                                }
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.webhook_url").value("https://example.com/hooks/batch"))
                .andExpect(jsonPath("$.user_id").value(userId.toString()));

        String webhookType = jdbc.sql("select webhook_type from webhooks where webhook_url = ?")
                .param(1, "https://example.com/hooks/batch")
                .query(String.class)
                .single();
        assertThat(webhookType).isEqualTo("BATCH_COMPLETED");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getWebhookById_ShouldReturnWebhook() throws Exception {
        Long webhookId = webhookIdByUrl("https://example.com/hooks/pending");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/webhooks/{id}", webhookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(webhookId.toString()))
                .andExpect(jsonPath("$.webhook_url").value("https://example.com/hooks/pending"))
                .andExpect(jsonPath("$.webhook_type").value("PENDING"))
                .andExpect(jsonPath("$.user_id").value(userId.toString()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getWebhookByNonExistingId_ShouldReturnError() throws Exception {
        long invalidId = Long.MAX_VALUE;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/webhooks/{id}", invalidId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(
                        String.format("Webhook with id %s not found", invalidId))));
    }

    @Test
    @WithMockUser(roles = "USER")
    void update_ShouldUpdateWebhookSuccessfully() throws Exception {
        Long webhookId = webhookIdByUrl("https://example.com/hooks/pending");

        mockMvc.perform(put("/api/webhooks/{id}", webhookId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "webhookURL": "https://example.com/hooks/updated",
                                    "userID": "%s",
                                    "webhookType": "BATCH_COMPLETED"
                                }
                                """.formatted(userId)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("https://example.com/hooks/updated")));

        String webhookType = jdbc.sql("select webhook_type from webhooks where id = ?")
                .param(1, webhookId)
                .query(String.class)
                .single();
        assertThat(webhookType).isEqualTo("BATCH_COMPLETED");

        String webhookUrl = jdbc.sql("select webhook_url from webhooks where id = ?")
                .param(1, webhookId)
                .query(String.class)
                .single();
        assertThat(webhookUrl).isEqualTo("https://example.com/hooks/updated");
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUnexistingWebhook_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from webhooks")
                .query(Integer.class)
                .single();

        long invalidId = Long.MAX_VALUE;

        mockMvc.perform(put("/api/webhooks/{id}", invalidId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "webhookURL": "https://example.com/hooks/updated",
                                    "userID": "%s",
                                    "webhookType": "BATCH_COMPLETED"
                                }
                                """.formatted(userId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format("Webhook with id: %s not found", invalidId))));

        int rowsChanged = jdbc.sql("select count(*) from webhooks")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_ShouldDeleteWebhookSuccessfully() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from webhooks")
                .query(Integer.class)
                .single();

        Long webhookId = webhookIdByUrl("https://example.com/hooks/pending");

        mockMvc.perform(delete("/api/webhooks/{id}", webhookId).with(csrf()))
                .andExpect(status().isNoContent());

        int rowsChanged = jdbc.sql("select count(*) from webhooks")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore - 1);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUnexistingWebhook_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from webhooks")
                .query(Integer.class)
                .single();

        long invalidId = Long.MAX_VALUE;

        mockMvc.perform(delete("/api/webhooks/{id}", invalidId).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format("Webhook with id: %s not found", invalidId))));

        int rowsChanged = jdbc.sql("select count(*) from webhooks")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    private Long webhookIdByUrl(String url) {
        return jdbc.sql("select id from webhooks where webhook_url = ?")
                .param(1, url)
                .query(Long.class)
                .single();
    }
}
