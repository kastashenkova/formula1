package org.processing.service;

import java.util.List;
import org.processing.dto.BatchRequestDto;
import org.processing.dto.BatchResponseDto;

public interface BatchService {
    BatchResponseDto uploadBatch(BatchRequestDto batchDTO);
    List<BatchResponseDto> getBatches(int page, int size);
}
