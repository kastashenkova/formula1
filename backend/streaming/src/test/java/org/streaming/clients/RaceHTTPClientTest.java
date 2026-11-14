package org.streaming.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.streaming.dto.DriverApiResponseDto;
import org.streaming.dto.LocationApiResponseDto;
import org.streaming.dto.RaceApiResponseDto;
import org.streaming.exception.ExternalHTTPError;
import org.streaming.exception.RaceNotFound;

class RaceHTTPClientTest {

    private static final String BASE_URL = "https://openf1.test/v1";

    private MockRestServiceServer server;

    private RaceHTTPClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RaceHTTPClient(builder, BASE_URL);
    }

    @Test
    void fetchSession_ShouldQueryRaceByCountryAndYear() {
        server.expect(requestTo(BASE_URL + "/sessions?year=2024&country_name=Monaco&session_name=Race"))
                .andRespond(withSuccess("""
                        [{"session_key": 9523, "session_name": "Race", "date_start": "2024-05-26T13:00:00+00:00"}]
                        """, MediaType.APPLICATION_JSON));

        Optional<RaceApiResponseDto> session = client.fetchSession(2024, "Monaco");

        assertThat(session).contains(new RaceApiResponseDto(9523, OffsetDateTime.parse("2024-05-26T13:00:00+00:00")));
        server.verify();
    }

    @Test
    void fetchSession_ShouldReturnEmpty_WhenNoRaceFound() {
        server.expect(requestTo(BASE_URL + "/sessions?year=2024&country_name=Atlantis&session_name=Race"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(client.fetchSession(2024, "Atlantis")).isEmpty();
    }

    @Test
    void fetchDrivers_ShouldMapDrivers() {
        server.expect(requestTo(BASE_URL + "/drivers?session_key=9523"))
                .andRespond(withSuccess("""
                        [{"session_key": 9523, "driver_number": 1, "full_name": "Max VERSTAPPEN", "team_name": "Red Bull Racing"}]
                        """, MediaType.APPLICATION_JSON));

        List<DriverApiResponseDto> drivers = client.fetchDrivers(9523);

        assertThat(drivers).containsExactly(new DriverApiResponseDto(1L, "Max VERSTAPPEN"));
    }

    @Test
    void fetchLocations_ShouldMapTelemetryInUtc() {
        server.expect(requestTo(BASE_URL + "/location?session_key=9523&driver_number=1"))
                .andRespond(withSuccess("""
                        [{"date": "2024-05-26T15:08:08.143000+02:00", "x": 10, "y": -20, "z": 0, "driver_number": 1}]
                        """, MediaType.APPLICATION_JSON));

        List<LocationApiResponseDto> locations = client.fetchLocations(9523, 1L);

        assertThat(locations).hasSize(1);
        assertThat(locations.getFirst().toEntity().getTimestamp())
                .isEqualTo(LocalDateTime.of(2024, 5, 26, 13, 8, 8, 143_000_000));
        assertThat(locations.getFirst().toEntity().getY()).isEqualTo(-20);
    }

    @Test
    void notFound_ShouldThrowRaceNotFound() {
        server.expect(requestTo(BASE_URL + "/drivers?session_key=1"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(RaceNotFound.class, () -> client.fetchDrivers(1));
    }

    @Test
    void tooManyRequests_ShouldThrowExternalHTTPError() {
        server.expect(requestTo(BASE_URL + "/drivers?session_key=1"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThrows(ExternalHTTPError.class, () -> client.fetchDrivers(1));
    }

    @Test
    void serverError_ShouldThrowExternalHTTPError() {
        server.expect(requestTo(BASE_URL + "/location?session_key=1&driver_number=1"))
                .andRespond(withServerError());

        assertThrows(ExternalHTTPError.class, () -> client.fetchLocations(1, 1L));
    }
}
