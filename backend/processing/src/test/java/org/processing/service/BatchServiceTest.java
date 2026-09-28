package org.processing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.processing.dto.BatchRequestDto;
import org.processing.entity.BatchEntity;
import org.processing.exception.DuplicateBatchException;
import org.processing.repository.BatchRepository;
import org.processing.service.internal.BatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class BatchServiceTest {

    @Mock
    private BatchRepository repo;

    @Mock
    private ApplicationEventPublisher publisher;

    private BatchService service;

    @BeforeEach
    void setUp() {
        service = new BatchServiceImpl(repo, publisher);
    }

    @Test
    @DisplayName("Should throw on duplicate batches")
    void uploadRaceWithDuplicateUUID() {
        var uuid = UUID.randomUUID();
        var entity = new BatchEntity(
                uuid,
                "Monaco",
                2025,
                LocalDateTime.now(),
                null);
        var request = new BatchRequestDto(
                uuid,
                "Monaco",
                2025,
                null,
                null
        );
        when(repo.findById(any(UUID.class))).thenReturn(Optional.of(entity));

        assertThrows(
                DuplicateBatchException.class,
                () -> service.uploadBatch(request)
        );

        verify(repo, never()).save(any(BatchEntity.class));
    }

    @Test
    @DisplayName("Success case for batch upload")
    void uploadSuccessCaseSetCreatedAt() {
        var request = new BatchRequestDto(
                null,
                "Monaco",
                2025,
                null,
                null
        );
        when(repo.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(repo.save(any(BatchEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.uploadBatch(request);

        assertNotNull(response.batchId());
        assertNotNull(response.createdAt());
        verify(repo).save(any(BatchEntity.class));
    }

    @Test
    @DisplayName("Success request for filled batch list")
    void getBatchesSuccessfully() {
        var monacoRace = new BatchEntity(
                null,
                "Monaco",
                2025,
                null,
                null
        );

        var austriaRace = new BatchEntity(
                null,
                "Austria",
                2026,
                null,
                null
        );

        List<BatchEntity> races =  List.of(monacoRace, austriaRace);

        when(repo.findAll(any(Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(races));

        Pageable pageable = PageRequest.of(0, 10);
        var response = service.getBatches(pageable);

        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals("Monaco", response.getContent().getFirst().raceName());
        assertEquals("Austria", response.getContent().get(1).raceName());
        verify(repo).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Success request for empty batch list")
    void getEmptyPageSuccessfully() {
        when(repo.findAll(any(Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        Pageable pageable = PageRequest.of(0, 10);
        var response = service.getBatches(pageable);

        assertNotNull(response);
        assertEquals(0, response.getContent().size());
        verify(repo).findAll(any(Pageable.class));
    }
}
