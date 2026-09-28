package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.identity.validation.OnCreate;
import org.identity.validation.OnUpdate;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.streaming.dto.DriverRequestDto;
import org.streaming.dto.DriverResponseDto;
import org.streaming.dto.RaceRequestDto;
import org.streaming.dto.RaceResponseDto;
import org.streaming.service.RaceService;

@RestController
@Tag(name = "Race controller",
        description = "Endpoints for managing races")
@RequestMapping("/races")
public class RaceController {

    private final RaceService raceService;

    public RaceController(RaceService raceService) {
        this.raceService = raceService;
    }

    @PostMapping
    @Operation(summary = "Race creation",
            description = "Create a new race")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RaceResponseDto> create(@Validated(OnCreate.class)
                                                    @RequestBody RaceRequestDto requestDto) {
        RaceResponseDto created = raceService.addRace(requestDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.raceId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/{id}")
    @Operation(summary = "Race creation",
            description = "Create a new race")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RaceResponseDto> update(@PathVariable UUID id,
                                                  @Validated(OnUpdate.class)
                                                  @RequestBody RaceRequestDto requestDto) {
        RaceResponseDto updated = raceService.updateRace(id, requestDto);

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}")
    @Operation(summary = "Get race",
            description = "Get an existing race by its id")
    public ResponseEntity<RaceResponseDto> getRace(@PathVariable UUID id) {
        RaceResponseDto race = raceService.getRace(id);

        return ResponseEntity.ok(race);
    }

    @PostMapping
    @Operation(summary = "Get races",
            description = "Get all existing races")
    public ResponseEntity<List<RaceResponseDto>> getRaces(Pageable pageable) {
        List<RaceResponseDto> races = raceService.getRaces(pageable);

        return ResponseEntity.ok(races);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a certain race",
            description = "Delete a race by its id")
    public ResponseEntity<Void> deleteRace(@PathVariable UUID id) {
        raceService.deleteRace(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/driver")
    @Operation(summary = "Add driver",
            description = "Add driver to the existing race")
    public ResponseEntity<DriverResponseDto> addDriver(
            @PathVariable UUID id,
            @Valid @RequestBody DriverRequestDto request
    ) {
        DriverResponseDto created = raceService.addDriver(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}/driver")
    @Operation(summary = "Get drivers",
            description = "Get drivers of the existing race")
    public List<DriverResponseDto> getDrivers(@PathVariable UUID id) {
        return raceService.getDrivers(id);
    }

    @DeleteMapping("/{raceId}/driver/{driverId}")
    @Operation(summary = "Delete driver",
            description = "Delete driver from the existing race")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDriver(@PathVariable UUID raceId, @PathVariable UUID driverId) {
        raceService.deleteDriver(raceId, driverId);
    }
}
