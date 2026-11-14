package org.streaming.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.streaming.dto.DriverApiResponseDto;
import org.streaming.dto.LocationApiResponseDto;
import org.streaming.dto.RaceApiResponseDto;
import org.streaming.exception.ExternalHTTPError;
import org.streaming.exception.RaceNotFound;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class RaceHTTPClient {

    private final RestClient restClient;

    public RaceHTTPClient(RestClient.Builder restClientBuilder,
                          @Value("${openf1.base-url:https://api.openf1.org/v1}") String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public Optional<RaceApiResponseDto> fetchSession(Integer year, String raceName) {
        List<RaceApiResponseDto> responseList = get(
                uriBuilder -> uriBuilder
                        .path("/sessions")
                        .queryParam("year", year)
                        .queryParam("country_name", raceName)
                        .queryParam("session_name", "Race")
                        .build(),
                String.format("Race in %s in %d is not found", raceName, year),
                new ParameterizedTypeReference<>() {
                });

        return responseList.stream().findFirst();
    }

    public List<DriverApiResponseDto> fetchDrivers(Integer sessionKey) {
        return get(
                uriBuilder -> uriBuilder
                        .path("/drivers")
                        .queryParam("session_key", sessionKey)
                        .build(),
                String.format("Drivers for session %d are not found", sessionKey),
                new ParameterizedTypeReference<>() {
                });
    }

    public List<LocationApiResponseDto> fetchLocations(Integer sessionKey, Long driverNumber) {
        return get(
                uriBuilder -> uriBuilder
                        .path("/location")
                        .queryParam("session_key", sessionKey)
                        .queryParam("driver_number", driverNumber)
                        .build(),
                String.format("Telemetry for driver %d in session %d is not found", driverNumber, sessionKey),
                new ParameterizedTypeReference<>() {
                });
    }

    private <T> List<T> get(Function<UriBuilder, URI> uri,
                            String notFoundMessage,
                            ParameterizedTypeReference<List<T>> type) {
        List<T> responseList = this.restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(status -> status.isSameCodeAs(HttpStatus.NOT_FOUND), (request, response) -> {
                    throw new RaceNotFound(notFoundMessage);
                })
                .onStatus(status -> status.is5xxServerError() || status.is4xxClientError(), (request, response) -> {
                    throw new ExternalHTTPError(String.format("Receive unexpected error from api, status %s", response.getStatusCode()));
                })
                .body(type);

        return responseList != null ? responseList : List.of();
    }
}
