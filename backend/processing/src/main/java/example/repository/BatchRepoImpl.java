package example.repository;

import example.dto.Batch;
import example.entity.BatchEntity;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class BatchRepoImpl implements BatchRepo{

    @Override
    public BatchEntity upload(Batch batch) {
        throw new NotImplementedException();
    }
}
