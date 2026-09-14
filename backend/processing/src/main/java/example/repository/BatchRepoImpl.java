package example.repository;

import example.dto.BatchDTO;
import example.entity.BatchEntity;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class BatchRepoImpl implements BatchRepo{

    @Override
    public BatchEntity upload(BatchDTO batchDTO) {
        throw new NotImplementedException();
    }
}
