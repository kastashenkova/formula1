package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.example.dto.race_session.RaceSessionResponseDto;
import org.example.service.race.RacesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Race management",
        description = "Endpoints for managing racing registration")
@RestController
@RequestMapping("/races")
public class RacesController {
    private final RacesService raceService;

    public RacesController(RacesService raceService) {
        this.raceService = raceService;
    }

    @GetMapping
    @Operation(summary = "All races",
            description = "Information about all the races")
    public ResponseEntity<List<RaceSessionResponseDto>> getRaces(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        List<RaceSessionResponseDto> list = raceService.getRaces(page, size);
        return ResponseEntity.ok(list);
    }
}
