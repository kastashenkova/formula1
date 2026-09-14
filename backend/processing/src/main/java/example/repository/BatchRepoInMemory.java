package example.repository;

import example.dto.BatchDTO;
import example.entity.BatchEntity;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Repository;

@Repository
public class BatchRepoInMemory implements BatchRepo{
    @Override
    public BatchEntity upload(BatchDTO batchDTO) {
        throw new NotImplementedException();
    }
}
