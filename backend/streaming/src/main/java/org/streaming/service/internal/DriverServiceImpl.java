package org.streaming.service.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.streaming.dto.DriverRequestDto;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.TelemetryPointRequestDto;
import org.streaming.dto.TelemetryPointResponseDto;
import org.streaming.entity.DriverEntity;
import org.streaming.entity.TelemetryPointEntity;
import org.streaming.repository.DriverRepository;
import org.streaming.repository.TelemetryPointRepository;
import org.streaming.service.DriverService;

@Service
@Transactional
public class DriverServiceImpl implements DriverService {
    private static final Logger log = LoggerFactory.getLogger(DriverServiceImpl.class);
    private final DriverRepository driverRepository;
    private final TelemetryPointRepository telemetryPointRepository;

    public DriverServiceImpl(DriverRepository driverRepository,
                             TelemetryPointRepository telemetryPointRepository) {
        this.driverRepository = driverRepository;
        this.telemetryPointRepository = telemetryPointRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponseDto getDriver(UUID driverId) {
        DriverEntity driver = driverRepository.findByIdWithDetails(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + driverId + " not found"));

        return DriverResponseDto.fromEntity(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponseDto> getDrivers(Pageable pageable) {
        return driverRepository.findAllWithDetails(pageable)
                .map(DriverResponseDto::fromEntity);
    }

    @Override
    public DriverResponseDto addDriver(DriverRequestDto driverRequestDto) {
        DriverEntity driver = DriverRequestDto.toEntity(driverRequestDto);
        DriverEntity saved = driverRepository.save(driver);

        log.info("Created driver {}", saved.getId());

        return DriverResponseDto.fromEntity(saved);
    }

    @Override
    public DriverResponseDto updateDriver(UUID id, DriverRequestDto driverRequestDto) {
        DriverEntity driver = driverRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + id + " not found"));

        driver.setDriverNumber(driverRequestDto.driverNumber());
        driver.setFullName(driverRequestDto.fullName());

        DriverEntity saved = driverRepository.save(driver);

        log.info("Updated driver {}", saved.getId());

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

        log.info("Added telemetry point {}", savedPoint.getId());

        return TelemetryPointResponseDto.fromEntity(savedPoint);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TelemetryPointResponseDto> getTelemetryPoints(UUID driverId, Pageable pageable) {
        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + driverId + " not found"));

        return telemetryPointRepository.findAllWithDetailsByDriverOrderByTimestamp(driver, pageable)
                .map(TelemetryPointResponseDto::fromEntity);
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
