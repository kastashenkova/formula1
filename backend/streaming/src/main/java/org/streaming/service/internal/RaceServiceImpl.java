package org.streaming.service.internal;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.audit.AuditAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.streaming.clients.RaceHTTPClient;
import org.streaming.dto.DriverRequestDto;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.RaceApiResponseDto;
import org.streaming.dto.RaceRequestDto;
import org.streaming.dto.RaceResponseDto;
import org.streaming.entity.DriverEntity;
import org.streaming.entity.RaceEntity;
import org.streaming.exception.DuplicateRaceException;
import org.streaming.exception.RaceNotFound;
import org.streaming.repository.DriverRepository;
import org.streaming.repository.RaceRepository;
import org.streaming.service.RaceService;

@Service
@Transactional
@Validated
public class RaceServiceImpl implements RaceService {
    private static final Logger log = LoggerFactory.getLogger(RaceServiceImpl.class);
    private final RaceRepository raceRepository;

    private final DriverRepository driverRepository;

    private final RaceHTTPClient raceHTTPClient;

    private final RaceDataImporter raceDataImporter;

    public RaceServiceImpl(RaceRepository raceRepository,
                           DriverRepository driverRepository,
                           RaceHTTPClient raceHTTPClient,
                           RaceDataImporter raceDataImporter) {
        this.raceRepository = raceRepository;
        this.driverRepository = driverRepository;
        this.raceHTTPClient = raceHTTPClient;
        this.raceDataImporter = raceDataImporter;
    }

    @Override
    @Transactional(readOnly = true)
    public RaceResponseDto getRace(UUID raceId) {
        RaceEntity raceEntity = raceRepository.findByIdWithDetails(raceId)
                .orElseThrow(() -> new EntityNotFoundException("Race with id: " + raceId + " not found"));

        return RaceResponseDto.fromEntity(raceEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RaceResponseDto> getRaces(Pageable pageable) {
        return raceRepository.findAllWithDetails(pageable)
                .map(RaceResponseDto::fromEntity);
    }

    @Override
    @AuditAction(action = "RACE_CREATION")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public RaceResponseDto addRace(RaceRequestDto raceRequestDto) {
        RaceApiResponseDto session = raceHTTPClient.fetchSession(raceRequestDto.year(), raceRequestDto.raceName())
                .orElseThrow(() -> new RaceNotFound(String.format("Race in %s in %d is not found",
                        raceRequestDto.raceName(), raceRequestDto.year())));

        if (raceRepository.existsBySessionKey(session.sessionKey())) {
            throw new DuplicateRaceException(String.format("Race in %s in %d already exists",
                    raceRequestDto.raceName(), raceRequestDto.year()));
        }

        LocalDateTime raceDate = session.startDate().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        RaceEntity saved = raceRepository.save(
                new RaceEntity(raceRequestDto.raceName(), raceDate, session.sessionKey()));

        log.info("Created race {}, importing data for session {}", saved.getRaceId(), session.sessionKey());

        try {
            raceDataImporter.importRaceData(saved.getRaceId(), session.sessionKey());
        } catch (RuntimeException e) {
            log.warn("Import of race {} failed, removing it", saved.getRaceId(), e);
            raceRepository.deleteById(saved.getRaceId());
            throw e;
        }

        return RaceResponseDto.fromEntity(saved);
    }

    @Override
    @AuditAction(action = "RACE_DELETE")
    public void deleteRace(UUID id) {
        raceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Race with id: " + id + " not found"));

        raceRepository.deleteById(id);
    }

    @Override
    @AuditAction(action = "RACE_DRIVER_CREATION")
    public DriverResponseDto addDriver(UUID raceId, DriverRequestDto request) {
        RaceEntity raceEntity = raceRepository.findById(raceId)
                .orElseThrow(() -> new EntityNotFoundException("Race with id: " + raceId + " not found"));

        DriverEntity driverEntity = DriverRequestDto.toEntity(request);
        raceEntity.addDriver(driverEntity);
        DriverEntity savedDriver = driverRepository.save(driverEntity);

        log.info("Added driver {}", savedDriver.getId());

        return DriverResponseDto.fromEntity(savedDriver);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponseDto> getDrivers(UUID raceId, Pageable pageable) {
        RaceEntity raceEntity = raceRepository.findById(raceId)
                .orElseThrow(() -> new EntityNotFoundException("Race with id: " + raceId + " not found"));

        return driverRepository.findByRaceOrderByFullName(raceEntity, pageable)
                .map(DriverResponseDto::fromEntity);
    }

    @Override
    @AuditAction(action = "RACE_DRIVER_DELETE")
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
