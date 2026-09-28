package org.streaming.service.internal;

import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.streaming.dto.*;
import org.streaming.entity.DriverEntity;
import org.streaming.entity.TelemetryPointEntity;
import org.streaming.repository.DriverRepository;
import org.streaming.repository.TelemetryPointRepository;
import org.streaming.service.DriverService;

@Service
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final TelemetryPointRepository telemetryPointRepository;

    public DriverServiceImpl(DriverRepository driverRepository,
                             TelemetryPointRepository telemetryPointRepository) {
        this.driverRepository = driverRepository;
        this.telemetryPointRepository = telemetryPointRepository;
    }

    @Override
    public DriverResponseDto getDriver(UUID driverId) {
        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + driverId + " not found"));

        return DriverResponseDto.fromEntity(driver);
    }

    @Override
    public List<DriverResponseDto> getDrivers() {
        return driverRepository.findAll()
                .stream()
                .map(DriverResponseDto::fromEntity)
                .toList();
    }

    @Override
    public DriverResponseDto addDriver(DriverRequestDto driverRequestDto) {
        DriverEntity driver = DriverRequestDto.toEntity(driverRequestDto);
        DriverEntity saved = driverRepository.save(driver);

        return DriverResponseDto.fromEntity(saved);
    }

    @Override
    public DriverResponseDto updateDriver(UUID id, DriverRequestDto driverRequestDto) {
        DriverEntity driver = driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + id + " not found"));

        driver.setDriverNumber(driverRequestDto.driverNumber());
        driver.setFullName(driverRequestDto.fullName());

        DriverEntity saved = driverRepository.save(driver);

        return DriverResponseDto.fromEntity(saved);
    }

    @Override
    public void deleteDriver(UUID id) {
        driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + id + " not found"));

        driverRepository.deleteById(id);
    }

    @Override
    public TelemetryPointResponseDto addTelemetryPoint(UUID driverId, TelemetryPointRequestDto request) {
        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + driverId + " not found"));

        TelemetryPointEntity telemetryPointEntity = TelemetryPointRequestDto.toEntity(request);
        driver.addTelemetryPoint(telemetryPointEntity);
        TelemetryPointEntity savedPoint = telemetryPointRepository.save(telemetryPointEntity);

        return TelemetryPointResponseDto.fromEntity(savedPoint);
    }

    @Override
    public List<TelemetryPointResponseDto> getTelemetryPoints(UUID driverId) {
        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + driverId + " not found"));

        return telemetryPointRepository.findAllByDriverOrderByTimestamp(driver)
                .stream()
                .map(TelemetryPointResponseDto::fromEntity)
                .toList();
    }

    @Override
    public void deleteTelemetryPoint(UUID driverId, UUID telemetryPointId) {
        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + driverId + " not found"));

        TelemetryPointEntity telemetryPointEntity = telemetryPointRepository.findById(telemetryPointId)
                .orElseThrow(() -> new EntityNotFoundException("TelemetryPoint with id "
                        + telemetryPointId + " not found"));

        if (!telemetryPointEntity.getDriver().equals(driver)) {
            String message = String.format("Telemetry Point with id %s does not belong to the Driver with id %s",
                    telemetryPointId, driverId);
            throw new IllegalArgumentException(message);
        }

        driver.removeTelemetryPoint(telemetryPointEntity);
    }
}
