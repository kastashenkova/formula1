package org.example.service;

import org.example.dto.BatchRequestDto;
import org.example.dto.BatchResponseDto;
import java.util.List;

public interface BatchService {
    BatchResponseDto uploadBatch(BatchRequestDto batchDTO);
    List<BatchResponseDto> getBatches();
}
