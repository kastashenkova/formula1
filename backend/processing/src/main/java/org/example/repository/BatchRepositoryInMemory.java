package org.example.repository;

import org.example.dto.BatchRequestDto;
import org.example.entity.BatchEntity;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Repository;

@Repository
public class BatchRepositoryInMemory implements BatchRepository {
    @Override
    public BatchEntity upload(BatchRequestDto batchDTO) {
        throw new NotImplementedException();
    }
}
