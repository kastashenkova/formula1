package org.streaming.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.streaming.entity.DriverEntity;
import org.streaming.entity.TelemetryPointEntity;

@Repository
public interface TelemetryPointRepository extends JpaRepository<TelemetryPointEntity, UUID> {

    @Query("""
            SELECT p FROM TelemetryPointEntity p
            JOIN FETCH p.driver
            WHERE p.id = :id
            """)
    Optional<TelemetryPointEntity> findByIdWithDetails(@Param("id") UUID id);

    @Query(
            value = """
                    SELECT p
                    FROM TelemetryPointEntity p
                    JOIN FETCH p.driver
                    """,
            countQuery = """
                    SELECT count(p)
                    FROM TelemetryPointEntity p
                    """
    )
    Page<TelemetryPointEntity> findAllWithDetails(Pageable pageable);

    @Query(
            value = """
                    SELECT p
                    FROM TelemetryPointEntity p
                    JOIN FETCH p.driver
                    WHERE p.driver = :driver
                    ORDER BY p.timestamp ASC
                    """,
            countQuery = """
                    SELECT count(p)
                    FROM TelemetryPointEntity p
                    WHERE p.driver = :driver
                    """
    )
    Page<TelemetryPointEntity> findAllWithDetailsByDriverOrderByTimestamp(@Param("driver") DriverEntity driver,
                                                                          Pageable pageable);
}
