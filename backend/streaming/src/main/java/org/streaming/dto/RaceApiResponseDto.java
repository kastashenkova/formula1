package org.streaming.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RaceApiResponseDto (
        @JsonProperty("session_key")
        Integer sessionKey,
        @JsonProperty("date_start")
        OffsetDateTime startDate
        // We can add more field if needed. Check for full response `README.md`
) {
}
