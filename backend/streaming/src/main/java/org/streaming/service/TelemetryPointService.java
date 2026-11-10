package org.streaming.service;

import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.streaming.dto.TelemetryPointRequestDto;
import org.streaming.dto.TelemetryPointResponseDto;
import org.streaming.validation.OnUpdate;

@Validated
public interface TelemetryPointService {
    TelemetryPointResponseDto getTelemetryPoint(UUID pointId);
    Page<TelemetryPointResponseDto> getTelemetryPoints(Pageable pageable);

    @Validated(OnUpdate.class)
    TelemetryPointResponseDto updateTelemetryPoint(UUID pointId, @Valid TelemetryPointRequestDto requestDto);

    void deleteTelemetryPoint(UUID pointId);
}
