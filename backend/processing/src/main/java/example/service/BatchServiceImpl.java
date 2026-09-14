package example.service;

import example.dto.BatchRequestDto;
import example.dto.BatchResponseDto;
import example.repository.BatchRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepo;

    public BatchServiceImpl(BatchRepository batchRepo) {
        this.batchRepo = batchRepo;
    }

    public BatchResponseDto upload(BatchRequestDto requestDto) {
        return new BatchResponseDto(
                UUID.randomUUID(),
                requestDto.raceName(),
                requestDto.year(),
                LocalDateTime.now(),
                null
        );
    }
}
