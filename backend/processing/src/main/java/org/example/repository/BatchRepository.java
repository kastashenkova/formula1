package org.example.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.entity.BatchEntity;

public interface BatchRepository {
    BatchEntity save(BatchEntity batch);
    Optional<BatchEntity> findById(UUID id);
    List<BatchEntity> findAll(int page, int size);
}
