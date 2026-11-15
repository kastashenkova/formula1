package org.streaming.service.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.streaming.clients.RaceHTTPClient;
import org.streaming.dto.RaceApiResponseDto;
import org.streaming.dto.RaceImportedEvent;
import org.streaming.dto.RaceRequestDto;
import org.streaming.dto.RaceResponseDto;
import org.streaming.entity.RaceEntity;
import org.streaming.exception.DuplicateRaceException;
import org.streaming.exception.ExternalHTTPError;
import org.streaming.exception.RaceNotFound;
import org.streaming.repository.DriverRepository;
import org.streaming.repository.RaceRepository;

@ExtendWith(MockitoExtension.class)
class RaceServiceImplTest {

    private static final String REQUESTED_BY = "admin@ukma.edu.ua";
    private static final RaceRequestDto REQUEST = new RaceRequestDto(null, "Monaco", 2024);
    private static final RaceApiResponseDto SESSION =
            new RaceApiResponseDto(9523, OffsetDateTime.parse("2024-05-26T15:00:00+02:00"));

    @Mock
    private RaceRepository raceRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RaceHTTPClient raceHTTPClient;

    @Mock
    private RaceDataImporter raceDataImporter;

    @Mock
    private RaceDataCleaner raceDataCleaner;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PlatformTransactionManager transactionManager;

    private RaceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RaceServiceImpl(raceRepository, driverRepository, raceHTTPClient, raceDataImporter, raceDataCleaner,
                eventPublisher, new TransactionTemplate(transactionManager));
    }

    @Test
    void addRace_ShouldSaveRaceWithProviderDateAndImportData() {
        UUID raceId = UUID.randomUUID();
        when(raceHTTPClient.fetchSession(2024, "Monaco")).thenReturn(Optional.of(SESSION));
        when(raceRepository.save(any(RaceEntity.class))).thenAnswer(invocation -> {
            RaceEntity race = invocation.getArgument(0);
            race.setRaceId(raceId);
            return race;
        });

        RaceResponseDto response = service.addRace(REQUEST, REQUESTED_BY);

        ArgumentCaptor<RaceEntity> captor = ArgumentCaptor.forClass(RaceEntity.class);
        verify(raceRepository).save(captor.capture());
        assertThat(captor.getValue().getSessionKey()).isEqualTo(9523);
        assertThat(response).isEqualTo(new RaceResponseDto(raceId, "Monaco", LocalDateTime.of(2024, 5, 26, 13, 0)));
        verify(raceDataImporter).importRaceData(raceId, 9523);
        verify(eventPublisher).publishEvent(
                RaceImportedEvent.success(raceId, "Monaco", LocalDateTime.of(2024, 5, 26, 13, 0), REQUESTED_BY));
    }

    @Test
    void addRace_ShouldThrowRaceNotFound_WhenProviderHasNoRace() {
        when(raceHTTPClient.fetchSession(2024, "Monaco")).thenReturn(Optional.empty());

        assertThrows(RaceNotFound.class, () -> service.addRace(REQUEST, REQUESTED_BY));

        verify(raceRepository, never()).save(any());
    }

    @Test
    void addRace_ShouldThrowDuplicate_WhenSessionAlreadyImported() {
        when(raceHTTPClient.fetchSession(2024, "Monaco")).thenReturn(Optional.of(SESSION));
        when(raceRepository.existsBySessionKey(9523)).thenReturn(true);

        assertThrows(DuplicateRaceException.class, () -> service.addRace(REQUEST, REQUESTED_BY));

        verify(raceRepository, never()).save(any());
    }

    @Test
    void addRace_ShouldRemoveRace_WhenImportFails() {
        UUID raceId = UUID.randomUUID();
        when(raceHTTPClient.fetchSession(2024, "Monaco")).thenReturn(Optional.of(SESSION));
        when(raceRepository.save(any(RaceEntity.class))).thenAnswer(invocation -> {
            RaceEntity race = invocation.getArgument(0);
            race.setRaceId(raceId);
            return race;
        });
        doThrow(new ExternalHTTPError("boom")).when(raceDataImporter).importRaceData(raceId, 9523);

        assertThrows(ExternalHTTPError.class, () -> service.addRace(REQUEST, REQUESTED_BY));

        verify(raceDataCleaner).deleteRace(raceId);
        verify(eventPublisher).publishEvent(
                RaceImportedEvent.failed(raceId, "Monaco", LocalDateTime.of(2024, 5, 26, 13, 0), REQUESTED_BY, "boom"));
    }

    @Test
    void addRace_ShouldThrowDuplicate_WhenConcurrentRequestInsertedSameSession() {
        when(raceHTTPClient.fetchSession(2024, "Monaco")).thenReturn(Optional.of(SESSION));
        when(raceRepository.save(any(RaceEntity.class)))
                .thenThrow(new DataIntegrityViolationException("uk_races_session_key"));

        assertThrows(DuplicateRaceException.class, () -> service.addRace(REQUEST, REQUESTED_BY));

        verify(raceDataImporter, never()).importRaceData(any(), any());
    }
}
