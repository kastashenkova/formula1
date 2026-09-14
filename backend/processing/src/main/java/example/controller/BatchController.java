package example.controller;

import example.dto.BatchRequestDto;
import example.dto.BatchResponseDto;
import example.service.BatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.validation.OnCreate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        var newBatch = batchService.upload(requestDto);
        return ResponseEntity.ok(newBatch);
    }

}
