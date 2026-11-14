package org.streaming.service.internal;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.streaming.clients.RaceHTTPClient;
import org.streaming.dto.DriverApiResponseDto;
import org.streaming.dto.LocationApiResponseDto;
import org.streaming.entity.DriverEntity;
import org.streaming.entity.RaceEntity;
import org.streaming.exception.ExternalHTTPError;
import org.streaming.repository.DriverRepository;
import org.streaming.repository.RaceRepository;

/**
 * Loads drivers and their telemetry for an already saved race.
 * Kept as a separate bean so it can be switched to background execution with {@code @Async}.
 */
@Component
public class RaceDataImporter {
    private static final Logger log = LoggerFactory.getLogger(RaceDataImporter.class);
    private final RaceHTTPClient raceHTTPClient;
    private final RaceRepository raceRepository;
    private final DriverRepository driverRepository;
    private final TransactionTemplate transactionTemplate;
    private final long requestDelayMs;

    public RaceDataImporter(RaceHTTPClient raceHTTPClient,
                            RaceRepository raceRepository,
                            DriverRepository driverRepository,
                            TransactionTemplate transactionTemplate,
                            @Value("${openf1.request-delay-ms:400}") long requestDelayMs) {
        this.raceHTTPClient = raceHTTPClient;
        this.raceRepository = raceRepository;
        this.driverRepository = driverRepository;
        this.transactionTemplate = transactionTemplate;
        this.requestDelayMs = requestDelayMs;
    }

    public void importRaceData(UUID raceId, Integer sessionKey) {
        List<DriverApiResponseDto> drivers = raceHTTPClient.fetchDrivers(sessionKey);
        long totalPoints = 0;

        for (DriverApiResponseDto driver : drivers) {
            pause();
            List<LocationApiResponseDto> locations = raceHTTPClient.fetchLocations(sessionKey, driver.driverNumber());
            totalPoints += locations.size();

            // One transaction per driver keeps the persistence context small (~25k points per driver)
            transactionTemplate.executeWithoutResult(status -> saveDriver(raceId, driver, locations));

            log.info("Imported driver {} with {} telemetry points for race {}",
                    driver.driverNumber(), locations.size(), raceId);
        }

        log.info("Fetched {} telemetry points for {} drivers of race {} (session {})",
                totalPoints, drivers.size(), raceId, sessionKey);
    }

    private void saveDriver(UUID raceId, DriverApiResponseDto driver, List<LocationApiResponseDto> locations) {
        RaceEntity race = raceRepository.getReferenceById(raceId);
        DriverEntity driverEntity = driver.toEntity();
        driverEntity.setRace(race);
        locations.forEach(location -> driverEntity.addTelemetryPoint(location.toEntity()));

        driverRepository.save(driverEntity);
    }

    private void pause() {
        try {
            Thread.sleep(requestDelayMs);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new ExternalHTTPError("Race data import was interrupted");
        }
    }
}
