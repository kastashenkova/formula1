package org.streaming.service;

import java.util.List;
import java.util.UUID;
import org.streaming.dto.DriverRequestDto;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.TelemetryPointRequestDto;
import org.streaming.dto.TelemetryPointResponseDto;

public interface DriverService {
    DriverResponseDto getDriver(UUID driverId);
    List<DriverResponseDto> getDrivers();
    DriverResponseDto addDriver(DriverRequestDto driverRequestDto);
    DriverResponseDto updateDriver(UUID id, DriverRequestDto driverRequestDto);
    void deleteDriver(UUID id);
    TelemetryPointResponseDto addTelemetryPoint(UUID driverId, TelemetryPointRequestDto request);
    List<TelemetryPointResponseDto> getTelemetryPoints(UUID driverId);
    void deleteTelemetryPoint(UUID driverId, UUID telemetryPointId);
}
