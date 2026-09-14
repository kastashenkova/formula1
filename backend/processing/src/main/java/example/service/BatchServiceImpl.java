package example.service;

import example.dto.BatchDTO;
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

    public BatchDTO upload(BatchDTO batchDTO) {
        return new BatchDTO(
                UUID.randomUUID(),
                batchDTO.raceName(),
                batchDTO.year(),
                LocalDateTime.now(),
                null
        );
    }
}
