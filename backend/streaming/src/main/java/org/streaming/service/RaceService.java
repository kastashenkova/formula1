package org.streaming.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.streaming.dto.DriverRequestDto;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.RaceRequestDto;
import org.streaming.dto.RaceResponseDto;

public interface RaceService {
    RaceResponseDto getRace(UUID raceId);
    Page<RaceResponseDto> getRaces(Pageable pageable);
    RaceResponseDto addRace(RaceRequestDto raceRequestDto);
    RaceResponseDto updateRace(UUID id, RaceRequestDto raceRequestDto);
    void deleteRace(UUID id);
    DriverResponseDto addDriver(UUID raceId, DriverRequestDto request);
    Page<DriverResponseDto> getDrivers(UUID raceId, Pageable pageable);
    void deleteDriver(UUID raceId, UUID driverId);
}
