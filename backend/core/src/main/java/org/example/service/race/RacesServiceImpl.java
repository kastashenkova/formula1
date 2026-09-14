package org.example.service.race;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.example.dto.race_session.RaceSessionRequestDto;
import org.example.dto.race_session.RaceSessionResponseDto;
import org.example.entity.RaceSessionEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RacesServiceImpl implements RacesService {
    private final List<RaceSessionEntity> raceSessionEntities = new CopyOnWriteArrayList<>();

    @Override
    public List<RaceSessionResponseDto> getRaces(int page, int size) {
        return raceSessionEntities
                .stream()
                .skip((long) Math.max(0, page) * Math.max(1, size))
                .limit(Math.max(1, size))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RaceSessionResponseDto addRace(RaceSessionRequestDto requestDto) {
        RaceSessionEntity newRace = new RaceSessionEntity(
                //TODO
        );
        raceSessionEntities.add(newRace);
        return mapToResponse(newRace);
    }

    private RaceSessionResponseDto mapToResponse(RaceSessionEntity entity) {
        return new RaceSessionResponseDto(
                //TODO
        );
    }
}
