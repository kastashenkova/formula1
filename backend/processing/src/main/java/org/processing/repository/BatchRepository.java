package org.processing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.processing.entity.BatchEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchRepository {
    BatchEntity save(BatchEntity batch);
    Optional<BatchEntity> findById(UUID id);
    List<BatchEntity> findAll(int page, int size);

    Page<BatchEntity> findAll(Pageable pageable);
}
