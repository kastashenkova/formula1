package org.streaming.service;

import java.util.List;
import java.util.UUID;
import org.streaming.dto.DriverRequestDto;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.RaceRequestDto;
import org.streaming.dto.RaceResponseDto;

public interface RaceService {
    RaceResponseDto getRace(UUID raceId);
    List<RaceResponseDto> getRaces();
    RaceResponseDto addRace(RaceRequestDto raceRequestDto);
    RaceResponseDto updateRace(UUID id, RaceRequestDto raceRequestDto);
    void deleteRace(UUID id);
    DriverResponseDto addDriver(UUID raceId, DriverRequestDto request);
    List<DriverResponseDto> getDrivers(UUID raceId);
    void deleteDriver(UUID raceId, UUID driverId);
}
