package org.example.service;

import org.example.command.UpdateBatchStatusCommand;
import org.example.dto.BatchRequestDto;
import org.example.dto.BatchResponseDto;
import java.util.List;
import java.util.UUID;

public interface BatchService {
    BatchResponseDto uploadBatch(BatchRequestDto batchDTO);
    List<BatchResponseDto> getBatches(int page, int size);

    BatchResponseDto updateStatus(UUID id, UpdateBatchStatusCommand command);
}
