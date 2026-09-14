package org.example.service.race;

import java.util.List;

import org.example.dto.race_session.RaceSessionRequestDto;
import org.example.dto.race_session.RaceSessionResponseDto;

public interface RacesService {
    RaceSessionResponseDto addRace(RaceSessionRequestDto requestDto);
    List<RaceSessionResponseDto> getRaces(int page, int size);
}
