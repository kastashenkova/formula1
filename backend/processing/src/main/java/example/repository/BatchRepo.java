package example.repository;

import example.dto.BatchDTO;
import example.entity.BatchEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchRepo {
    BatchEntity upload(BatchDTO batchDTO);
}
