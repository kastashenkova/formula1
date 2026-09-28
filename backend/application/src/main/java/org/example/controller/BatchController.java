package org.example.controller;

import org.processing.dto.BatchRequestDto;
import org.processing.dto.BatchResponseDto;
import org.processing.service.BatchService;
import org.processing.validation.OnCreate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Batch controller",
        description = "Controller for upload race info")
@RestController
@RequestMapping("/batches")
public class BatchController {
    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping
    @Operation(summary = "Create a new race batch",
            description = "Create a new batch for the race")
    public ResponseEntity<BatchResponseDto> uploadRace(@Validated(OnCreate.class)
                                                           @RequestBody BatchRequestDto requestDto) {
        var newBatch = batchService.uploadBatch(requestDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newBatch.batchId())
                .toUri();

        return ResponseEntity.created(location).body(newBatch);
    }

    @GetMapping
    @Operation(summary = "All batches",
            description = "Information about all the race batches")
    public ResponseEntity<List<BatchResponseDto>> getBatches(Pageable pageable) {
        List<BatchResponseDto> list = batchService.getBatches(pageable);
        return ResponseEntity.ok(list);
    }
}
