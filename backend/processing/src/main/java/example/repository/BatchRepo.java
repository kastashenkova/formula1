package example.repository;

import example.dto.Batch;
import example.entity.BatchEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchRepo {
    BatchEntity upload(Batch batch);
}
