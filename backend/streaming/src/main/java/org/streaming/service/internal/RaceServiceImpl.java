package org.streaming.service.internal;

import com.sun.jdi.request.DuplicateRequestException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.streaming.dto.DriverRequestDto;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.RaceRequestDto;
import org.streaming.dto.RaceResponseDto;
import org.streaming.entity.DriverEntity;
import org.streaming.entity.RaceEntity;
import org.streaming.repository.DriverRepository;
import org.streaming.repository.RaceRepository;
import org.streaming.service.RaceService;

@Service
public class RaceServiceImpl implements RaceService {

    private final RaceRepository raceRepository;

    private final DriverRepository driverRepository;

    public RaceServiceImpl(RaceRepository raceRepository, DriverRepository driverRepository) {
        this.raceRepository = raceRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public RaceResponseDto getRace(UUID raceId) {
        RaceEntity raceEntity = raceRepository.findById(raceId)
                .orElseThrow(() -> new EntityNotFoundException("Race with id: " + raceId + " not found"));

        return RaceResponseDto.fromEntity(raceEntity);
    }

    @Override
    public List<RaceResponseDto> getRaces(Pageable pageable) {
        return raceRepository.findAll(pageable)
                .stream()
                .map(RaceResponseDto::fromEntity)
                .toList();
    }

    @Override
    public RaceResponseDto addRace(RaceRequestDto raceRequestDto) {
        if (raceRepository.existsByRaceName(raceRequestDto.raceName())) {
            String message = String.format("Race with name %s already exists", raceRequestDto.raceName());
            throw new DuplicateRequestException(message);
        }

        RaceEntity race = RaceRequestDto.toEntity(raceRequestDto);
        RaceEntity saved = raceRepository.save(race);

        return RaceResponseDto.fromEntity(saved);
    }

    @Override
    public RaceResponseDto updateRace(UUID id, RaceRequestDto raceRequestDto) {
        RaceEntity raceEntity = raceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Race with id: " + id + " not found"));

        raceEntity.setRaceName(raceRequestDto.raceName());
        raceEntity.setRaceDate(raceRequestDto.raceDate());

        raceRepository.save(raceEntity);

        return RaceResponseDto.fromEntity(raceEntity);
    }

    @Override
    public void deleteRace(UUID id) {
        raceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Race with id: " + id + " not found"));

        raceRepository.deleteById(id);
    }

    @Override
    public DriverResponseDto addDriver(UUID raceId, DriverRequestDto request) {
        RaceEntity raceEntity = raceRepository.findById(raceId)
                .orElseThrow(() -> new EntityNotFoundException("Race with id: " + raceId + " not found"));

        DriverEntity driverEntity = DriverRequestDto.toEntity(request);
        raceEntity.addDriver(driverEntity);
        DriverEntity savedDriver = driverRepository.save(driverEntity);

        return DriverResponseDto.fromEntity(savedDriver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponseDto> getDrivers(UUID raceId) {
        RaceEntity raceEntity = raceRepository.findById(raceId)
                .orElseThrow(() -> new EntityNotFoundException("Race with id: " + raceId + " not found"));

        return driverRepository.findByRaceOrderByFullName(raceEntity)
                .stream()
                .map(DriverResponseDto::fromEntity)
                .toList();
    }

    @Override
    public void deleteDriver(UUID raceId, UUID driverId) {
        RaceEntity race = raceRepository.findById(raceId)
                .orElseThrow(() -> new EntityNotFoundException("Race with id: " + raceId + " not found"));

        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id: " + driverId + " not found"));

        if (!driver.getRace().equals(race)) {
            String message = String.format("Driver with id %s does not belong to the Race with id %s",
                    driverId, raceId);
            throw new IllegalArgumentException(message);
        }

        race.removeDriver(driver);
    }
}
