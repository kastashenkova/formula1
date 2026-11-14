package org.example.integration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.example.support.IntegrationTest;
import org.example.support.OpenF1StubConfiguration;
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
    void create_ShouldStoreRaceWithDriversAndTelemetryFromProvider() throws Exception {
        mockMvc.perform(post("/api/races")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "Monaco",
                                    "year": 2024
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.race_date").value("2024-05-26T13:00:00"));

        UUID raceId = raceIdByName("Monaco");

        Integer sessionKey = jdbc.sql("select session_key from races where race_id = ?")
                .param(1, raceId)
                .query(Integer.class)
                .single();
        assertThat(sessionKey).isEqualTo(OpenF1StubConfiguration.MONACO_SESSION_KEY);

        List<String> drivers = jdbc.sql("select full_name from drivers where race_id = ? order by driver_number")
                .param(1, raceId)
                .query(String.class)
                .list();
        assertThat(drivers).containsExactly("Max VERSTAPPEN", "Charles LECLERC");

        Integer points = jdbc.sql("""
                        select count(*) from telemetry_points p
                        join drivers d on d.id = p.driver_id
                        where d.race_id = ?
                        """)
                .param(1, raceId)
                .query(Integer.class)
                .single();
        assertThat(points).isEqualTo(3);

        LocalDateTime firstPoint = jdbc.sql("""
                        select min(p.timestamp) from telemetry_points p
                        join drivers d on d.id = p.driver_id
                        where d.driver_number = 1
                        """)
                .query(LocalDateTime.class)
                .single();
        assertThat(firstPoint).isEqualTo(LocalDateTime.of(2024, 5, 26, 13, 0, 0, 143_000_000));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDuplicateRace_ShouldReturnConflict() throws Exception {
        String body = """
                {
                    "raceName": "Monaco",
                    "year": 2024
                }
                """;

        mockMvc.perform(post("/api/races").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/races").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Race in Monaco in 2024 already exists")));

        Integer races = jdbc.sql("select count(*) from races where race_name = ?")
                .param(1, "Monaco")
                .query(Integer.class)
                .single();
        assertThat(races).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRaceUnknownToProvider_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(post("/api/races").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "Atlantis",
                                    "year": 2024
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Race in Atlantis in 2024 is not found")));

        Integer races = jdbc.sql("select count(*) from races where race_name = ?")
                .param(1, "Atlantis")
                .query(Integer.class)
                .single();
        assertThat(races).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRaceWhenImportFails_ShouldReturnBadGatewayAndNotStoreRace() throws Exception {
        mockMvc.perform(post("/api/races").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "raceName": "Imola",
                                    "year": 2024
                                }
                                """))
                .andExpect(status().isBadGateway());

        Integer races = jdbc.sql("select count(*) from races where race_name = ?")
                .param(1, "Imola")
                .query(Integer.class)
                .single();
        assertThat(races).isZero();
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
                                    "year": 2020
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
                                    "raceName": "Monaco",
                                    "year": 2024
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
