package org.streaming.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public record RaceApiResponseDto (
        @JsonProperty("session_key")
        Integer sessionKey,
        @JsonProperty("date_start")
        String startDate //ISO 8601 format
        // We can add more field if needed. Check for full response `README.md`
) {
}

