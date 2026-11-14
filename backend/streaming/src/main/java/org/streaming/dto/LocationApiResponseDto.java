package org.streaming.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.streaming.entity.TelemetryPointEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LocationApiResponseDto(
        @JsonProperty("date")
        OffsetDateTime date,
        @JsonProperty("x")
        Integer x,
        @JsonProperty("y")
        Integer y
) {
    public TelemetryPointEntity toEntity() {
        return new TelemetryPointEntity(x, y, date.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
    }
}
