package org.example.e2e.thymeleaf;

import org.example.support.E2ETest;
import org.identity.dto.UserLoginResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.streaming.dto.RaceResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
public class RaceAPIE2ETest {

    @Autowired
    private RestTestClient client;

    @Autowired
    private JdbcClient jdbc;

    private String login(String username, String password) {
        UserLoginResponseDto response = client.post().uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "email": "%s",
                            "password": "%s"
                        }
                      """
                        .formatted(username, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserLoginResponseDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).isNotNull();
        return response.token();
    }

    @Test
    void adminCreatesRace_ThenViewerReadsIt() {
        String adminToken = login("astashenkova.katya@gmail.com", "admin123");

        RaceResponseDto createdRace = client.post().uri("/api/races")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                           "raceName": "Monaco Grand Prix 2026",
                           "raceDate": "2026-06-07T16:00:00Z"
                        }
                      """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RaceResponseDto.class)
                .returnResult()
                .getResponseBody();

        UUID newRaceId = createdRace.raceId();
        assertThat(jdbc.sql("select count(*) from races where race_id = ?")
                .param(1, newRaceId)
                .query(Integer.class)
                .single())
                .isEqualTo(1);

        String userToken = login("k.astashenkova@ukma.edu.ua", "kateryna123");
        RaceResponseDto raceResponseDto = client.get().uri("/api/races/{id}", newRaceId)
                .headers(headers -> headers.setBearerAuth(userToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(RaceResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(raceResponseDto)
                .isEqualTo(new RaceResponseDto(
                        newRaceId,
                        "Monaco Grand Prix 2026",
                        LocalDateTime.of(2026, 6, 7, 16, 0,0)));
    }

    @Test
    void createRaceWithInvalidFields_ShouldReturnConflict() {
        String adminToken = login("astashenkova.katya@gmail.com", "admin123");

        client.post().uri("/api/races")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                           "raceName": "",
                           "raceDate": "2027-06-07T16:00:00Z"
                        }
                      """)
                .exchange()
                .expectStatus().isEqualTo(400)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.detail").exists();
    }

    @Test
    void userToken_ShouldNotCreateRace() {
        String viewerToken = login("k.astashenkova@ukma.edu.ua", "kateryna123");

        client.post().uri("/api/races")
                .headers(headers -> headers.setBearerAuth(viewerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                           "raceName": "Monaco Grand Prix 2026",
                           "raceDate": "2026-06-07T16:00:00Z"
                        }
                        """)
                .exchange()
                .expectStatus().isForbidden();
    }
}
