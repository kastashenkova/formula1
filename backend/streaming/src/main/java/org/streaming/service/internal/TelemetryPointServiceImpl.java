package org.streaming.service.internal;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.streaming.dto.TelemetryPointRequestDto;
import org.streaming.dto.TelemetryPointResponseDto;
import org.streaming.entity.TelemetryPointEntity;
import org.streaming.repository.TelemetryPointRepository;
import org.streaming.service.TelemetryPointService;

@Service
@Transactional
public class TelemetryPointServiceImpl implements TelemetryPointService {

    private final TelemetryPointRepository telemetryPointRepository;

    public TelemetryPointServiceImpl(TelemetryPointRepository telemetryPointRepository) {
        this.telemetryPointRepository = telemetryPointRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TelemetryPointResponseDto getTelemetryPoint(UUID pointId) {
        TelemetryPointEntity telemetryPointEntity = telemetryPointRepository.findByIdWithDetails(pointId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Telemetry Point with id: " + pointId + " not found"));

        return TelemetryPointResponseDto.fromEntity(telemetryPointEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TelemetryPointResponseDto> getTelemetryPoints(Pageable pageable) {
        return telemetryPointRepository.findAllWithDetails(pageable)
                .map(TelemetryPointResponseDto::fromEntity);
    }

    @Override
    public TelemetryPointResponseDto updateTelemetryPoint(UUID pointId, TelemetryPointRequestDto requestDto) {
        TelemetryPointEntity telemetryPointEntity = telemetryPointRepository.findByIdWithDetails(pointId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Telemetry Point with id: " + pointId + " not found"));

        telemetryPointEntity.setX(requestDto.x());
        telemetryPointEntity.setY(requestDto.y());
        telemetryPointEntity.setTimestamp(requestDto.timestamp());
        telemetryPointEntity.setSpeed(requestDto.speed());

        telemetryPointRepository.save(telemetryPointEntity);

        return TelemetryPointResponseDto.fromEntity(telemetryPointEntity);
    }

    @Override
    public void deleteTelemetryPoint(UUID pointId) {
        telemetryPointRepository.findById(pointId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Telemetry Point with id: " + pointId + " not found"));

        telemetryPointRepository.deleteById(pointId);
    }
}
