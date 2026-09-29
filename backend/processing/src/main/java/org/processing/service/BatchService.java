package org.processing.service;

import org.processing.dto.BatchRequestDto;
import org.processing.dto.BatchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BatchService {
    BatchResponseDto uploadBatch(BatchRequestDto batchDTO);
    Page<BatchResponseDto> getBatches(Pageable pageable);
}
