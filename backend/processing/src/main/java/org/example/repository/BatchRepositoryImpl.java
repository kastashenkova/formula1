package org.example.repository;

import org.example.dto.BatchRequestDto;
import org.example.entity.BatchEntity;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class BatchRepositoryImpl implements BatchRepository {

    @Override
    public BatchEntity upload(BatchRequestDto requestDto) {
        throw new NotImplementedException();
    }
}
