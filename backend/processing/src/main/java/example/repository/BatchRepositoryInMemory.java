package example.repository;

import example.dto.BatchRequestDto;
import example.entity.BatchEntity;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Repository;

@Repository
public class BatchRepositoryInMemory implements BatchRepository {
    @Override
    public BatchEntity upload(BatchRequestDto batchDTO) {
        throw new NotImplementedException();
    }
}
