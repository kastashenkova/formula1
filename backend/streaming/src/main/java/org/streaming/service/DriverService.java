package org.streaming.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.streaming.dto.DriverRequestDto;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.TelemetryPointRequestDto;
import org.streaming.dto.TelemetryPointResponseDto;

public interface DriverService {
    DriverResponseDto getDriver(UUID driverId);
    Page<DriverResponseDto> getDrivers(Pageable pageable);
    DriverResponseDto addDriver(DriverRequestDto driverRequestDto);
    DriverResponseDto updateDriver(UUID id, DriverRequestDto driverRequestDto);
    void deleteDriver(UUID id);
    TelemetryPointResponseDto addTelemetryPoint(UUID driverId, TelemetryPointRequestDto request);
    Page<TelemetryPointResponseDto> getTelemetryPoints(UUID driverId, Pageable pageable);
    void deleteTelemetryPoint(UUID driverId, UUID telemetryPointId);
}
