package org.example.integration;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.example.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class AuditIntegrationTest {

    private static final String ADMIN_EMAIL = "audit.admin@ukma.edu.ua";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        jdbc.sql("INSERT INTO users (id, email, phone_number, password, role, user_status) VALUES (?, ?, ?, ?, ?, ?)")
                .param(1, adminId)
                .param(2, ADMIN_EMAIL)
                .param(3, "+380980137099")
                .param(4, "password")
                .param(5, "ADMIN")
                .param(6, "ACTIVE")
                .update();
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void successfulOperation_ShouldStoreSuccessfulAuditRecord() throws Exception {
        mockMvc.perform(post("/api/races")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "Audit Grand Prix",
                                    "raceDate": "2026-10-23T15:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated());

        Map<String, Object> audit = awaitSingleAuditRecord("RACE_CREATION");

        assertThat(audit.get("success")).isEqualTo(true);
        assertThat(audit.get("user_id")).isEqualTo(adminId);
        assertThat((String) audit.get("method_name")).contains("addRace");
        assertThat((String) audit.get("arguments")).contains("Audit Grand Prix");
        assertThat((String) audit.get("result")).contains("Audit Grand Prix");
        assertThat(audit.get("error_message")).isNull();
        assertThat(audit.get("timestamp")).isNotNull();
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void businessRuleViolation_ShouldStoreFailedAuditRecordAndKeepOriginalError() throws Exception {
        UUID missingRaceId = UUID.randomUUID();

        mockMvc.perform(delete("/api/races/{id}", missingRaceId).with(csrf()))
                .andExpect(status().isNotFound());

        Map<String, Object> audit = awaitSingleAuditRecord("RACE_DELETE");

        assertThat(audit.get("success")).isEqualTo(false);
        assertThat(audit.get("user_id")).isEqualTo(adminId);
        assertThat((String) audit.get("method_name")).contains("deleteRace");
        assertThat((String) audit.get("arguments")).contains(missingRaceId.toString());
        assertThat(audit.get("result")).isNull();
        assertThat((String) audit.get("error_message"))
                .isEqualTo("Race with id: " + missingRaceId + " not found");
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void validationError_ShouldStoreFailedAuditRecordAndReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/races")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "F1",
                                    "raceDate": "2028-10-23T15:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest());

        Map<String, Object> audit = awaitSingleAuditRecord("RACE_CREATION");

        assertThat(audit.get("success")).isEqualTo(false);
        assertThat(audit.get("error_message")).isNotNull();
        assertThat(jdbc.sql("select count(*) from races").query(Integer.class).single()).isZero();
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void invalidBatch_ShouldStoreFailedAuditRecordAndReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/batches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "exampleRace",
                                    "year": 2027
                                }
                                """))
                .andExpect(status().isBadRequest());

        Map<String, Object> audit = awaitSingleAuditRecord("BATCH_UPLOAD");

        assertThat(audit.get("success")).isEqualTo(false);
        assertThat(audit.get("error_message")).isNotNull();
    }

    @Test
    @WithAnonymousUser
    void failedLogin_ShouldStoreAuditRecordWithoutArguments() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "nobody@ukma.edu.ua",
                                    "password": "wrongPassword1"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        Map<String, Object> audit = awaitSingleAuditRecord("USER_AUTHENTICATION");

        assertThat(audit.get("success")).isEqualTo(false);
        assertThat(audit.get("user_id")).isNull();
        assertThat(audit.get("arguments")).isNull();
        assertThat(audit.get("error_message")).isNotNull();
    }

    private Map<String, Object> awaitSingleAuditRecord(String action) {
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(findAuditRecords(action)).hasSize(1));
        return findAuditRecords(action).getFirst();
    }

    private List<Map<String, Object>> findAuditRecords(String action) {
        return jdbc.sql("select * from audit_log where action = ?")
                .param(1, action)
                .query()
                .listOfRows();
    }
}
