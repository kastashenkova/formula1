package example.service;

import example.dto.BatchRequestDto;
import example.dto.BatchResponseDto;

public interface BatchService {
    BatchResponseDto upload(BatchRequestDto batchDTO);
}
