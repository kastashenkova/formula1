package org.streaming.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.streaming.entity.DriverEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DriverApiResponseDto(
        @JsonProperty("driver_number")
        Long driverNumber,
        @JsonProperty("full_name")
        String fullName
) {
    public DriverEntity toEntity() {
        return new DriverEntity(driverNumber, fullName);
    }
}
