package org.streaming.service;

import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.streaming.dto.DriverRequestDto;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.RaceRequestDto;
import org.streaming.dto.RaceResponseDto;
import org.streaming.validation.OnCreate;
import org.streaming.validation.OnUpdate;

@Validated
public interface RaceService {
    RaceResponseDto getRace(UUID raceId);
    Page<RaceResponseDto> getRaces(Pageable pageable);

    @Validated(OnCreate.class)
    RaceResponseDto addRace(@Valid RaceRequestDto raceRequestDto);

    @Validated(OnUpdate.class)
    RaceResponseDto updateRace(UUID id, @Valid RaceRequestDto raceRequestDto);
    void deleteRace(UUID id);

    @Validated(OnCreate.class)
    DriverResponseDto addDriver(UUID raceId, @Valid DriverRequestDto request);

    Page<DriverResponseDto> getDrivers(UUID raceId, Pageable pageable);
    void deleteDriver(UUID raceId, UUID driverId);
}
