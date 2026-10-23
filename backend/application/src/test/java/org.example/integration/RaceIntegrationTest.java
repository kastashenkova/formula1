package org.example.integration;

import java.time.LocalDateTime;
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
public class RaceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    @BeforeEach
    void setUp() {
        jdbc.sql("INSERT INTO races (race_id, race_name, race_date) VALUES (?, ?, ?)")
                .param(1, UUID.randomUUID())
                .param(2, "New York 2025")
                .param(3, LocalDateTime.parse("2025-09-23T17:00:00"))
                .update();

        jdbc.sql("INSERT INTO races (race_id, race_name, race_date) VALUES (?, ?, ?)")
                .param(1, UUID.randomUUID())
                .param(2, "New York 2026")
                .param(3, LocalDateTime.parse("2026-09-23T17:00:00"))
                .update();

        UUID raceId = jdbc.sql("select race_id from races where race_name = ?")
                .param(1, "New York 2025")
                .query(UUID.class)
                .single();

        jdbc.sql("INSERT INTO drivers (id, driver_number, full_name, race_id) VALUES (?, ?, ?, ?)")
                .param(1, UUID.randomUUID())
                .param(2, 22L)
                .param(3, "Oscar Piastri")
                .param(4, raceId)
                .update();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_ShouldStoreRow() throws Exception {
        mockMvc.perform(post("/api/races")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "Singapore Airlines Singapore GP",
                                    "raceDate": "2026-10-23T15:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated());

        String raceDate = jdbc.sql("select race_date from races where race_name = ? limit 1")
                .param(1, "Singapore Airlines Singapore GP")
                .query(String.class)
                .single();
        assertThat(raceDate).startsWith("2026-10-23");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRaceWithInvalidFields_ShouldShowFieldError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        mockMvc.perform(post("/api/races").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "F1",
                                    "raceDate": "2028-10-23T15:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation failed for one or more fields")));

        int rowsChanged = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "USER")
    void createRaceByNotAdmin_ShouldShowFieldError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        mockMvc.perform(post("/api/races").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "Singapore Airlines Singapore GP",
                                    "raceDate": "2026-10-23T15:00:00.000Z"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Access denied")));

        int rowsChanged = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRaces_ShouldShowAllExistingRaces() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/races"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("New York 2025")))
                .andExpect(content().string(containsString("New York 2026")));
    }

    @Test
    @WithAnonymousUser
    void getRacesWithoutAuthentication_ShouldShowError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/races"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRaces_ShouldShowEmptyList() throws Exception {
        jdbc.sql("delete from drivers").update();
        jdbc.sql("delete from races").update();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/races"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRaceById_ShouldReturnRace() throws Exception {
        UUID raceId = raceIdByName("New York 2025");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/races/{id}", raceId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("New York 2025")));
    }

    @Test
    @WithAnonymousUser
    void getRacesByIdWithoutAuthentication_ShouldShowError() throws Exception {
        UUID raceId = raceIdByName("New York 2025");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/races/{id}", raceId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRaceByNonExistingId_ShouldReturnError() throws Exception {
        UUID invalidId = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/races/{id}", invalidId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(
                        String.format("Race with id: %s not found", invalidId))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_ShouldUpdateRaceSuccessfully() throws Exception {
        UUID raceId = raceIdByName("New York 2025");

        mockMvc.perform(put("/api/races/{id}", raceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceId": "%s",
                                    "raceName": "New York 2025 Updated",
                                    "raceDate": "2025-10-23T15:00:00.000Z"
                                }
                                """.formatted(raceId)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("New York 2025 Updated")));

        LocalDateTime raceDate = jdbc.sql("select race_date from races where race_name = ?")
                .param(1, "New York 2025 Updated")
                .query(LocalDateTime.class)
                .single();
        assertThat(raceDate).isEqualTo(LocalDateTime.of(2025, 10, 23, 15, 0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUnexistingRace_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(put("/api/races/{id}", invalidId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceId": "%s",
                                    "raceName": "New York 2025 Updated",
                                    "raceDate": "2025-10-23T15:00:00.000Z"
                                }
                                """.formatted(invalidId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format("Race with id: %s not found", invalidId))));

        int rowsChanged = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateRaceByNotAdmin_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        UUID raceId = raceIdByName("New York 2025");

        mockMvc.perform(put("/api/races/{id}", raceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceId": "%s",
                                    "raceName": "New York 2025 Updated",
                                    "raceDate": "2025-10-23T15:00:00.000Z"
                                }
                                """.formatted(raceId)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Access denied")));

        int rowsChanged = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_ShouldDeleteRaceSuccessfully() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        UUID raceId = raceIdByName("New York 2025");

        mockMvc.perform(delete("/api/races/{id}", raceId).with(csrf()))
                .andExpect(status().isNoContent());

        int rowsChanged = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore - 1);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteRaceByNotAdmin_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        UUID raceId = raceIdByName("New York 2025");

        mockMvc.perform(delete("/api/races/{id}", raceId).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Access denied")));

        int rowsChanged = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUnexistingRace_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(delete("/api/races/{id}", invalidId).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format("Race with id: %s not found", invalidId))));

        int rowsChanged = jdbc.sql("select count(*) from races")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addDriverToUnexistingRace_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(post("/api/races/{id}/drivers", invalidId)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                    "driverNumber": "11",
                                    "fullName": "Max Verstappen"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format("Race with id: %s not found", invalidId))));

        int rowsChanged = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "USER")
    void addDriverByNotAdmin_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        UUID raceId = raceIdByName("New York 2025");

        mockMvc.perform(post("/api/races/{id}/drivers", raceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "driverNumber": "11",
                                    "fullName": "Max Verstappen"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Access denied")));

        int rowsChanged = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addDriverWithInvalidData_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        UUID raceId = raceIdByName("New York 2025");

        mockMvc.perform(post("/api/races/{id}/drivers", raceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "driverNumber": "1",
                                    "fullName": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation failed for one or more fields")));

        int rowsChanged = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addDriver_ShouldAddDriverSuccessfully() throws Exception {
        UUID raceId = raceIdByName("New York 2025");

        mockMvc.perform(post("/api/races/{id}/drivers", raceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "driverNumber": "11",
                                    "fullName": "Max Verstappen"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("Max Verstappen")));

        String fullName = jdbc.sql("select full_name from drivers where driver_number = ?")
                .param(1, 11)
                .query(String.class)
                .single();
        assertThat(fullName).isEqualTo("Max Verstappen");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getDrivers_ShouldShowAllExistingDriversForTheRace() throws Exception {
        UUID raceId = raceIdByName("New York 2025");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/races/{id}/drivers", raceId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Oscar Piastri")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getDrivers_ShouldShowEmptyList() throws Exception {
        UUID existingRaceId = raceIdByName("New York 2026");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/races/{id}/drivers", existingRaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithAnonymousUser
    void getDriversWithoutAuthentication_ShouldShowError() throws Exception {
        UUID existingRaceId = raceIdByName("New York 2025");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/races/{id}/drivers", existingRaceId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getDriversForUnexistingRace_ShouldShowError() throws Exception {
        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/races/{id}/drivers", invalidId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format("Race with id: %s not found", invalidId))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDriver_ShouldDeleteDriverSuccessfully() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        UUID existingRaceId = raceIdByName("New York 2025");

        UUID existingDriverId = driverIdByNumberAndRaceId(existingRaceId, 22);

        mockMvc.perform(delete("/api/races/{raceId}/drivers/{driverId}",
                        existingRaceId,existingDriverId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        int rowsChanged = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore - 1);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteDriverByNotAdmin_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        UUID existingRaceId = raceIdByName("New York 2025");

        UUID existingDriverId = driverIdByNumberAndRaceId(existingRaceId, 22);

        mockMvc.perform(delete("/api/races/{raceId}/drivers/{driverId}", existingRaceId, existingDriverId)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Access denied")));

        int rowsChanged = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDriverForUnexistingRace_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        UUID existingRaceId = raceIdByName("New York 2025");

        UUID existingDriverId = driverIdByNumberAndRaceId(existingRaceId, 22);

        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(delete("/api/races/{raceId}/drivers/{driverId}",
                        invalidId,
                        existingDriverId).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format("Race with id: %s not found", invalidId))));

        int rowsChanged = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUnexistingDriver_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        UUID existingRaceId = raceIdByName("New York 2025");

        UUID unexistingDriverId = UUID.randomUUID();

        mockMvc.perform(delete("/api/races/{raceId}/drivers/{driverId}",
                        existingRaceId,
                        unexistingDriverId).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format("Driver with id: %s not found", unexistingDriverId))));

        int rowsChanged = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDriverWhoNotBelongToTheRace_ShouldShowError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        UUID raceWithDriver = raceIdByName("New York 2025");
        UUID otherRace = raceIdByName("New York 2026");

        UUID existingDriverId = driverIdByNumberAndRaceId(raceWithDriver, 22);

        mockMvc.perform(delete("/api/races/{raceId}/drivers/{driverId}",
                        otherRace,
                        existingDriverId).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(
                        String.format("Driver with id %s does not belong to the Race with id %s", existingDriverId, otherRace))));

        int rowsChanged = jdbc.sql("select count(*) from drivers")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    private UUID raceIdByName(String name) {
        return jdbc.sql("select race_id from races where race_name = ?")
                .param(1, name)
                .query(UUID.class)
                .single();
    }

    private UUID driverIdByNumberAndRaceId(UUID raceId, long driverNumber) {
        return jdbc.sql("select id from drivers where driver_number = ? and race_id = ?")
                .param(1, driverNumber)
                .param(2, raceId)
                .query(UUID.class)
                .single();
    }
}
