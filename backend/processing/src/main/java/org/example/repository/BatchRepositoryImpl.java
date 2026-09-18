package org.example.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.example.entity.BatchEntity;
import org.example.enums.BatchStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class BatchRepositoryImpl implements BatchRepository {
    private final Map<UUID, BatchEntity> storage = new ConcurrentHashMap<>();

    @Override
    public BatchEntity save(BatchEntity batch) {
        storage.put(batch.batchId(), batch);
        return batch;
    }

    @Override
    public Optional<BatchEntity> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<BatchEntity> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public BatchEntity updateStatusById(UUID id, BatchStatus status) {
        BatchEntity existingBatch = storage.get(id);

        if (existingBatch == null) {
            return null;
        }

        BatchEntity updatedBatch = new BatchEntity(
                existingBatch.batchId(),
                existingBatch.raceName(),
                existingBatch.year(),
                existingBatch.createdAt(),
                existingBatch.deletedAt(),
                status
        );

        storage.put(id, updatedBatch);

        return updatedBatch;
    }
}
