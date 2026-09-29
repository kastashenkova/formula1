package org.streaming.service;

import java.util.List;
import java.util.UUID;
import org.streaming.dto.TelemetryPointRequestDto;
import org.streaming.dto.TelemetryPointResponseDto;

public interface TelemetryPointService {
    TelemetryPointResponseDto getTelemetryPoint(UUID pointId);
    List<TelemetryPointResponseDto> getTelemetryPoints();
    TelemetryPointResponseDto addTelemetryPoint(TelemetryPointRequestDto requestDto);
    TelemetryPointResponseDto updateTelemetryPoint(UUID pointId, TelemetryPointRequestDto requestDto);
    void deleteTelemetryPoint(UUID pointId);
}
