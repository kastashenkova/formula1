package example.repository;

import example.dto.BatchRequestDto;
import example.entity.BatchEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchRepository {
    BatchEntity upload(BatchRequestDto batchDTO);
}
