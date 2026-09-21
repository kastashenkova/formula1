package org.example.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.example.dto.BatchRequestDto;
import org.example.dto.BatchResponseDto;
import org.example.entity.BatchEntity;
import org.example.event.BatchCreatedEvent;
import org.example.exception.DuplicateBatchException;
import org.example.repository.BatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BatchServiceImpl implements BatchService {
    private static final Logger log = LoggerFactory.getLogger(BatchServiceImpl.class);
    private final BatchRepository batchRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BatchServiceImpl(BatchRepository batchRepo, ApplicationEventPublisher eventPublisher) {
        this.batchRepository = batchRepo;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public BatchResponseDto uploadBatch(BatchRequestDto requestDto) {
        UUID id = UUID.randomUUID();

        if (batchRepository.findById(id).isPresent()) {
            throw new DuplicateBatchException("Batch with id " + id + " already exists");
        }

        BatchEntity newBatch = new BatchEntity(
                id,
                requestDto.raceName(),
                requestDto.year(),
                LocalDateTime.now(),
                null);

        BatchEntity savedBatch = batchRepository.save(newBatch);

        eventPublisher.publishEvent(new BatchCreatedEvent(
                savedBatch.batchId(),
                savedBatch.raceName(),
                savedBatch.year(),
                savedBatch.createdAt(),
                savedBatch.deletedAt()
        ));

        log.info("Created batch {}", savedBatch.batchId());

        return mapToResponse(savedBatch);
    }

    @Override
    public List<BatchResponseDto> getBatches(int page, int size) {
        return batchRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BatchResponseDto mapToResponse(BatchEntity batchEntity) {
        return new BatchResponseDto(
                batchEntity.batchId(),
                batchEntity.raceName(),
                batchEntity.year(),
                batchEntity.createdAt(),
                batchEntity.deletedAt()
        );
    }
}
