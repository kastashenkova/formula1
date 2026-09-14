package example.repository;

import example.dto.Batch;
import example.entity.BatchEntity;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Repository;

@Repository
public class BatchRepoInMemory implements BatchRepo{
    @Override
    public BatchEntity upload(Batch batch) {
        throw new NotImplementedException();
    }
}
