package org.processing.service;

import java.util.List;
import org.processing.dto.BatchRequestDto;
import org.processing.dto.BatchResponseDto;
import org.springframework.data.domain.Pageable;

public interface BatchService {
    BatchResponseDto uploadBatch(BatchRequestDto batchDTO);
    List<BatchResponseDto> getBatches(Pageable pageable);
}
