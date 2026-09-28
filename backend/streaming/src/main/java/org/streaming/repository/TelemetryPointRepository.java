package org.streaming.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.streaming.entity.DriverEntity;
import org.streaming.entity.TelemetryPointEntity;

@Repository
public interface TelemetryPointRepository extends JpaRepository<TelemetryPointEntity, UUID> {

    List<TelemetryPointEntity> findAllByDriverOrderByTimestamp(DriverEntity driver);
}
