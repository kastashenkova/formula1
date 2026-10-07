package org.streaming.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.streaming.dto.TelemetryPointRequestDto;
import org.streaming.dto.TelemetryPointResponseDto;

public interface TelemetryPointService {
    TelemetryPointResponseDto getTelemetryPoint(UUID pointId);
    Page<TelemetryPointResponseDto> getTelemetryPoints(Pageable pageable);
    TelemetryPointResponseDto updateTelemetryPoint(UUID pointId, TelemetryPointRequestDto requestDto);
    void deleteTelemetryPoint(UUID pointId);
}
