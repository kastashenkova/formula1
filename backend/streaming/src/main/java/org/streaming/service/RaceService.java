package org.streaming.service;

import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.RaceRequestDto;
import org.streaming.dto.RaceResponseDto;
import org.streaming.validation.OnCreate;

@Validated
public interface RaceService {
    RaceResponseDto getRace(UUID raceId);
    Page<RaceResponseDto> getRaces(Pageable pageable);

    @Validated(OnCreate.class)
    RaceResponseDto addRace(@Valid RaceRequestDto raceRequestDto, String requestedBy);

    void deleteRace(UUID id);

    Page<DriverResponseDto> getDrivers(UUID raceId, Pageable pageable);
}
