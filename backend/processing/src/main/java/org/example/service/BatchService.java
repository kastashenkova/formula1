package org.example.service;

import java.util.List;
import org.example.dto.BatchRequestDto;
import org.example.dto.BatchResponseDto;

public interface BatchService {
    BatchResponseDto uploadBatch(BatchRequestDto batchDTO);
    List<BatchResponseDto> getBatches(int page, int size);
}
