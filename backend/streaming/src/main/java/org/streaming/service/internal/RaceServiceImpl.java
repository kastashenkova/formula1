package org.streaming.service.internal;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.audit.AuditAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;
import org.streaming.clients.RaceHTTPClient;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.RaceApiResponseDto;
import org.streaming.dto.RaceImportedEvent;
import org.streaming.dto.RaceRequestDto;
import org.streaming.dto.RaceResponseDto;
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

    private final RaceDataCleaner raceDataCleaner;

    private final ApplicationEventPublisher eventPublisher;

    private final TransactionTemplate transactionTemplate;

    public RaceServiceImpl(RaceRepository raceRepository,
                           DriverRepository driverRepository,
                           RaceHTTPClient raceHTTPClient,
                           RaceDataImporter raceDataImporter,
                           RaceDataCleaner raceDataCleaner,
                           ApplicationEventPublisher eventPublisher,
                           TransactionTemplate transactionTemplate) {
        this.raceRepository = raceRepository;
        this.driverRepository = driverRepository;
        this.raceHTTPClient = raceHTTPClient;
        this.raceDataImporter = raceDataImporter;
        this.raceDataCleaner = raceDataCleaner;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = transactionTemplate;
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
    public RaceResponseDto addRace(RaceRequestDto raceRequestDto, String requestedBy) {
        RaceApiResponseDto session = raceHTTPClient.fetchSession(raceRequestDto.year(), raceRequestDto.raceName())
                .orElseThrow(() -> new RaceNotFound(String.format("Race in %s in %d is not found",
                        raceRequestDto.raceName(), raceRequestDto.year())));

        String duplicateMessage = String.format("Race in %s in %d already exists",
                raceRequestDto.raceName(), raceRequestDto.year());

        if (raceRepository.existsBySessionKey(session.sessionKey())) {
            throw new DuplicateRaceException(duplicateMessage);
        }

        LocalDateTime raceDate = session.startDate().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        RaceEntity saved;
        try {
            saved = raceRepository.save(new RaceEntity(raceRequestDto.raceName(), raceDate, session.sessionKey()));
        } catch (DataIntegrityViolationException _) {
            // Concurrent request saved the same session between the check and the insert
            throw new DuplicateRaceException(duplicateMessage);
        }

        log.info("Created race {}, importing data for session {}", saved.getRaceId(), session.sessionKey());

        try {
            raceDataImporter.importRaceData(saved.getRaceId(), session.sessionKey());
        } catch (RuntimeException e) {
            log.warn("Import of race {} failed, removing it", saved.getRaceId(), e);
            raceDataCleaner.deleteRace(saved.getRaceId());
            publish(RaceImportedEvent.failed(saved.getRaceId(), saved.getRaceName(), raceDate, requestedBy,
                    e.getMessage()));
            throw e;
        }

        publish(RaceImportedEvent.success(saved.getRaceId(), saved.getRaceName(), raceDate, requestedBy));

        return RaceResponseDto.fromEntity(saved);
    }

    // Module listeners run only after commit, and the publication is stored in the same transaction
    private void publish(RaceImportedEvent event) {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(event));
    }

    @Override
    @AuditAction(action = "RACE_DELETE")
    public void deleteRace(UUID id) {
        if (!raceRepository.existsById(id)) {
            throw new EntityNotFoundException("Race with id: " + id + " not found");
        }

        raceDataCleaner.deleteRace(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponseDto> getDrivers(UUID raceId, Pageable pageable) {
        RaceEntity raceEntity = raceRepository.findById(raceId)
                .orElseThrow(() -> new EntityNotFoundException("Race with id: " + raceId + " not found"));

        return driverRepository.findByRaceOrderByFullName(raceEntity, pageable)
                .map(DriverResponseDto::fromEntity);
    }
}
