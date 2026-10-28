package org.example.integration;

import java.time.LocalDateTime;
import java.util.UUID;
import org.example.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class RaceFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_ShouldCreateRaceAndAddDriver_AndStoreInDatabase() throws Exception {
        mockMvc.perform(post("/api/races").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "Monaco Grand Prix 2026",
                                    "raceDate": "2026-06-07T16:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated());

        UUID newRaceId = jdbc.sql("select race_id from races where race_name = ?")
                .param(1, "Monaco Grand Prix 2026")
                .query(UUID.class)
                .single();

        assertThat(newRaceId).isNotNull();

        mockMvc.perform(post("/api/races/{id}/drivers", newRaceId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "driverNumber": "3",
                                    "fullName": "Max Verstappen"
                                }
                                """))
                .andExpect(status().isCreated());

        String driverName = jdbc.sql("select full_name from drivers where race_id = ? and driver_number = ?")
                .param(1, newRaceId)
                .param(2, 3)
                .query(String.class)
                .single();

        assertThat(driverName).isEqualTo("Max Verstappen");
    }

    @Test
    @WithMockUser(roles = "USER")
    void user_ShouldNotBeAbleToCreateRace() throws Exception {
        mockMvc.perform(post("/api/races").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "Monaco Grand Prix 2026",
                                    "raceDate": "2026-06-07T16:00:00Z"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Access denied")));

        Integer hackersFound = jdbc.sql("select count(*) from races where race_name = ?")
                .param(1, "Monaco Grand Prix 2026")
                .query(Integer.class)
                .single();

        assertThat(hackersFound).isZero();
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void anyUser_ShouldBeAbleToViewRaces() throws Exception {
        jdbc.sql("INSERT INTO races (race_id, race_name, race_date) VALUES (?, ?, ?)")
                .param(1, UUID.randomUUID())
                .param(2, "Monaco Grand Prix 2025")
                .param(3, LocalDateTime.parse("2025-10-28T15:00:00"))
                .update();

        mockMvc.perform(get("/api/races"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Monaco Grand Prix 2025")));
    }
}
