package org.example.dto.driver;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DriverResponseDto (
        @JsonProperty("driver_number")
        Long driverNumber,
        String code,
        @JsonProperty("full_name")
        String fullName
) {
}
