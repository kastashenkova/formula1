package org.streaming.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.streaming.entity.TelemetryPointEntity;

@Repository
public interface TelemetryPointRepository extends JpaRepository<TelemetryPointEntity, UUID> {

    @Modifying
    @Query("""
            DELETE FROM TelemetryPointEntity p
            WHERE p.driver.id IN (SELECT d.id FROM DriverEntity d WHERE d.race.raceId = :raceId)
            """)
    void deleteAllByRaceId(@Param("raceId") UUID raceId);
}
