package org.streaming.service;

import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.streaming.dto.DriverRequestDto;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.TelemetryPointRequestDto;
import org.streaming.dto.TelemetryPointResponseDto;
import org.streaming.validation.OnCreate;
import org.streaming.validation.OnUpdate;

@Validated
public interface DriverService {
    DriverResponseDto getDriver(UUID driverId);
    Page<DriverResponseDto> getDrivers(Pageable pageable);

    @Validated(OnCreate.class)
    DriverResponseDto addDriver(@Valid DriverRequestDto driverRequestDto);

    @Validated(OnUpdate.class)
    DriverResponseDto updateDriver(UUID id, @Valid DriverRequestDto driverRequestDto);
    void deleteDriver(UUID id);

    @Validated(OnCreate.class)
    TelemetryPointResponseDto addTelemetryPoint(UUID driverId, @Valid TelemetryPointRequestDto request);

    Page<TelemetryPointResponseDto> getTelemetryPoints(UUID driverId, Pageable pageable);
    void deleteTelemetryPoint(UUID driverId, UUID telemetryPointId);
}
