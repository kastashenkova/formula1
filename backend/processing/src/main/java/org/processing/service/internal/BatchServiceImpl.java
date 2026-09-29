package org.processing.service.internal;

import java.time.LocalDateTime;
import org.processing.dto.BatchRequestDto;
import org.processing.dto.BatchResponseDto;
import org.processing.entity.BatchEntity;
import org.processing.dto.BatchCreatedEvent;
import org.processing.repository.BatchRepository;
import org.processing.service.BatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        var newBatch = new BatchEntity(
                null,
                LocalDateTime.now(),
                null,
                null
        );
        var savedBatch = batchRepository.save(newBatch);

        eventPublisher.publishEvent(new BatchCreatedEvent(
                savedBatch.getBatchId(),
                newBatch.getCreatedAt()
        ));

        log.info("Created batch {}", savedBatch.getBatchId());

        return mapToResponse(savedBatch);
    }

    @Override
    public Page<BatchResponseDto> getBatches(Pageable pageable) {
        return batchRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private BatchResponseDto mapToResponse(BatchEntity batchEntity) {
        return new BatchResponseDto(
                batchEntity.getBatchId(),
                batchEntity.getCreatedAt(),
                batchEntity.getDeletedAt()
        );
    }
}
