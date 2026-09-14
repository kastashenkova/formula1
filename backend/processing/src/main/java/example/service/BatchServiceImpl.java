package example.service;

import example.dto.Batch;
import example.repository.BatchRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BatchServiceImpl implements BatchService {

    private final BatchRepo batchRepo;

    public BatchServiceImpl(BatchRepo batchRepo) {
        this.batchRepo = batchRepo;
    }

    public Batch upload(Batch batch) {
        return new Batch(
                UUID.randomUUID(),
                batch.raceName(),
                batch.year(),
                LocalDateTime.now(),
                null
        );
    }
}
