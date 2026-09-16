package org.example.repository;

import org.example.dto.BatchRequestDto;
import org.example.entity.BatchEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchRepository {
    BatchEntity upload(BatchRequestDto batchDTO);
}
