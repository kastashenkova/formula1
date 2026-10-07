package org.streaming.clients;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.streaming.dto.RaceApiResponseDto;
import org.streaming.exception.ExternalHTTPError;
import org.streaming.exception.RaceNotFound;

import java.util.List;
import java.util.Optional;

@Service
public class RaceHTTPClient {

    private final RestClient restClient;

    public RaceHTTPClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openf1.org/v1")
                .build();
    }

    public Optional<RaceApiResponseDto> fetchSession(Integer year, String raceName) {
        List<RaceApiResponseDto> responseList = this.restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sessions")
                        .queryParam("year", year)
                        .queryParam("session_name", raceName)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RaceNotFound(String.format("Race in %s in %d is not found", raceName, year));
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new ExternalHTTPError(String.format("Receive unexpected error from api, status %s", response.getStatusCode()));
                })
                .body(new ParameterizedTypeReference<>() {
                });

        return responseList != null && !responseList.isEmpty()
                ? Optional.of(responseList.getFirst())
                : Optional.empty();
    }
}
