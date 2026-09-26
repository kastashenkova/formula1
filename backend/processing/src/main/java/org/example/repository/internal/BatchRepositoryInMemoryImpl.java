package org.example.repository.internal;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.example.entity.BatchEntity;
import org.example.repository.BatchRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class BatchRepositoryInMemoryImpl implements BatchRepository {
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
    public List<BatchEntity> findAll(int page, int size) {
        return storage.values().stream()
                .sorted(Comparator.comparing(BatchEntity::createdAt))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }
}
