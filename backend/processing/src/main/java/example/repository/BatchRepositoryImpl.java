package example.repository;

import example.dto.BatchRequestDto;
import example.entity.BatchEntity;
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
