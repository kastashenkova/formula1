package org.streaming.service.internal;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.streaming.dto.TelemetryPointRequestDto;
import org.streaming.dto.TelemetryPointResponseDto;
import org.streaming.entity.TelemetryPointEntity;
import org.streaming.repository.TelemetryPointRepository;
import org.streaming.service.TelemetryPointService;

@Service
public class TelemetryPointServiceImpl implements TelemetryPointService {

    private final TelemetryPointRepository telemetryPointRepository;

    public TelemetryPointServiceImpl(TelemetryPointRepository telemetryPointRepository) {
        this.telemetryPointRepository = telemetryPointRepository;
    }

    @Override
    public TelemetryPointResponseDto getTelemetryPoint(UUID pointId) {
        TelemetryPointEntity telemetryPointEntity = telemetryPointRepository.findById(pointId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Telemetry Point with id: " + pointId + " not found"));

        return TelemetryPointResponseDto.fromEntity(telemetryPointEntity);
    }

    @Override
    public List<TelemetryPointResponseDto> getTelemetryPoints() {
        return telemetryPointRepository.findAll()
                .stream()
                .map(TelemetryPointResponseDto::fromEntity)
                .toList();
    }

    @Override
    public TelemetryPointResponseDto addTelemetryPoint(TelemetryPointRequestDto requestDto) {
        TelemetryPointEntity savedPoint = telemetryPointRepository.save(
                TelemetryPointRequestDto.toEntity(requestDto));

        return TelemetryPointResponseDto.fromEntity(savedPoint);
    }

    @Override
    public TelemetryPointResponseDto updateTelemetryPoint(UUID pointId, TelemetryPointRequestDto requestDto) {
        TelemetryPointEntity telemetryPointEntity = telemetryPointRepository.findById(pointId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Telemetry Point with id: " + pointId + " not found"));

        telemetryPointEntity.setX(requestDto.x());
        telemetryPointEntity.setY(requestDto.y());
        telemetryPointEntity.setTimestamp(requestDto.timestamp());

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
