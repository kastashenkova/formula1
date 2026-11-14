package org.streaming.service.internal;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.streaming.repository.DriverRepository;
import org.streaming.repository.RaceRepository;
import org.streaming.repository.TelemetryPointRepository;

/**
 * Removes races and drivers with bulk queries, so telemetry (~1M rows per race)
 * is not loaded into the persistence context by cascade removal.
 */
@Component
@Transactional
public class RaceDataCleaner {
    private final RaceRepository raceRepository;
    private final DriverRepository driverRepository;
    private final TelemetryPointRepository telemetryPointRepository;

    public RaceDataCleaner(RaceRepository raceRepository,
                           DriverRepository driverRepository,
                           TelemetryPointRepository telemetryPointRepository) {
        this.raceRepository = raceRepository;
        this.driverRepository = driverRepository;
        this.telemetryPointRepository = telemetryPointRepository;
    }

    public void deleteRace(UUID raceId) {
        telemetryPointRepository.deleteAllByRaceId(raceId);
        driverRepository.deleteAllByRaceId(raceId);
        raceRepository.deleteRaceById(raceId);
    }

    public void deleteDriver(UUID driverId) {
        telemetryPointRepository.deleteAllByDriverId(driverId);
        driverRepository.deleteDriverById(driverId);
    }
}
