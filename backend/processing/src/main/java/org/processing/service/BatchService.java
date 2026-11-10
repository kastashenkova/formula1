package org.processing.service;

import jakarta.validation.Valid;
import org.processing.dto.BatchRequestDto;
import org.processing.dto.BatchResponseDto;
import org.processing.validation.OnCreate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

@Validated
public interface BatchService {

    @Validated(OnCreate.class)
    BatchResponseDto uploadBatch(@Valid BatchRequestDto batchDTO);
    
    Page<BatchResponseDto> getBatches(Pageable pageable);
}
