package org.example.controller;

import org.example.dto.BatchRequestDto;
import org.example.dto.BatchResponseDto;
import org.example.service.BatchService;
import org.example.validation.OnCreate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<List<BatchResponseDto>> getRaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<BatchResponseDto> list = batchService.getBatches(page, size);
        return ResponseEntity.ok(list);
    }
}
