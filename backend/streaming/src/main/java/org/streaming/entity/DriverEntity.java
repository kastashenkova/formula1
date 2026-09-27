package org.streaming.entity;

public record DriverEntity(
        Long driverNumber,
        String code,
        String fullName
) {
}
