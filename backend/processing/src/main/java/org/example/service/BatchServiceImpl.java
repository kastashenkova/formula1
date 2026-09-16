package org.example.service;

import org.example.dto.BatchRequestDto;
import org.example.dto.BatchResponseDto;
import org.example.entity.BatchEntity;
import org.example.repository.BatchRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;

    // temporary decision instead of the db and repository layer
    private final List<BatchEntity> batchEntities = new CopyOnWriteArrayList<>();

    public BatchServiceImpl(BatchRepository batchRepo) {
        this.batchRepository = batchRepo;
    }

    public BatchResponseDto uploadBatch(BatchRequestDto requestDto) {
        BatchEntity newBatch = new BatchEntity(
                UUID.randomUUID(),
                requestDto.raceName(),
                requestDto.year(),
                LocalDateTime.now(),
                null);

        batchEntities.add(newBatch);

        return mapToResponse(newBatch);
    }

    @Override
    public List<BatchResponseDto> getBatches(int page, int size) {
        return batchEntities
                .stream()
                .skip((long) Math.max(0, page) * Math.max(1, size))
                .limit(Math.max(1, size))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BatchResponseDto mapToResponse(BatchEntity batchEntity) {
        return new BatchResponseDto(
                batchEntity.batch_id(),
                batchEntity.raceName(),
                batchEntity.year(),
                LocalDateTime.now(),
                null
        );
    }
}
