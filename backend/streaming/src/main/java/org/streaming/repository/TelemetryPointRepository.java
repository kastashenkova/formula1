package org.streaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.streaming.entity.TelemetryPointEntity;
import java.util.UUID;

@Repository
public interface TelemetryPointRepository extends JpaRepository<TelemetryPointEntity, UUID> {
}
