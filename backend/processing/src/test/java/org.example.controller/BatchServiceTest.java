package org.example.controller;

import org.example.dto.BatchRequestDto;
import org.example.entity.BatchEntity;
import org.example.exception.DuplicateBatchException;
import org.example.repository.BatchRepository;
import org.example.service.BatchService;
import org.example.service.BatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


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
}
