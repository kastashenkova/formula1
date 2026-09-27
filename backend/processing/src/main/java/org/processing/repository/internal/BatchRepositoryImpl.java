package org.processing.repository.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.NotImplementedException;
import org.processing.entity.BatchEntity;
import org.processing.repository.BatchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class BatchRepositoryImpl implements BatchRepository {

    @Override
    public BatchEntity save(BatchEntity batch) {
        throw new NotImplementedException();
    }

    @Override
    public Optional<BatchEntity> findById(UUID id) {
        throw new NotImplementedException();
    }

    @Override
    public List<BatchEntity> findAll(int page, int size) {
        throw new NotImplementedException();
    }
}
