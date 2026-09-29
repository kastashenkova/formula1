package org.streaming.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.streaming.entity.DriverEntity;

public record DriverResponseDto (
        @JsonProperty("driver_number")
        Long driverNumber,
        @JsonProperty("full_name")
        String fullName
) {
        public static DriverResponseDto fromEntity(DriverEntity driverEntity) {
                return new DriverResponseDto(
                        driverEntity.getDriverNumber(),
                        driverEntity.getFullName()
                );
        }
}
