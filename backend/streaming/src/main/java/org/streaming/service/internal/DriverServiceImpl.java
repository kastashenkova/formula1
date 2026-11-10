package org.streaming.service.internal;

import org.audit.AuditAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.streaming.dto.DriverRequestDto;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.TelemetryPointRequestDto;
import org.streaming.dto.TelemetryPointResponseDto;
import org.streaming.entity.DriverEntity;
import org.streaming.entity.TelemetryPointEntity;
import org.streaming.repository.DriverRepository;
import org.streaming.repository.TelemetryPointRepository;
import org.streaming.service.DriverService;
import org.streaming.validation.OnCreate;
import org.streaming.validation.OnUpdate;

@Service
@Transactional
@Validated
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
    @AuditAction(action = "DRIVER_CREATION")
    public DriverResponseDto addDriver(@Validated(OnCreate.class) DriverRequestDto driverRequestDto) {
        DriverEntity driver = DriverRequestDto.toEntity(driverRequestDto);
        DriverEntity saved = driverRepository.save(driver);

        log.info("Created driver {}", saved.getId());

        return DriverResponseDto.fromEntity(saved);
    }

    @Override
    @AuditAction(action = "DRIVER_UPDATE")
    public DriverResponseDto updateDriver(UUID id, @Validated(OnUpdate.class) DriverRequestDto driverRequestDto) {
        DriverEntity driver = driverRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + id + " not found"));

        driver.setDriverNumber(driverRequestDto.driverNumber());
        driver.setFullName(driverRequestDto.fullName());

        DriverEntity saved = driverRepository.save(driver);

        log.info("Updated driver {}", saved.getId());

        return DriverResponseDto.fromEntity(saved);
    }

    @Override
    @AuditAction(action = "DRIVER_DELETE")
    public void deleteDriver(UUID id) {
        driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + id + " not found"));

        driverRepository.deleteById(id);
    }

    @Override
    @AuditAction(action = "DRIVER_TELEMETRY_POINT_CREATION")
    public TelemetryPointResponseDto addTelemetryPoint(UUID driverId, @Validated(OnCreate.class) TelemetryPointRequestDto request) {
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
    @AuditAction(action = "DRIVER_TELEMETRY_POINT_DELETE")
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
