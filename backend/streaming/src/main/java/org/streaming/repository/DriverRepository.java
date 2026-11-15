package org.streaming.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.streaming.entity.DriverEntity;
import org.streaming.entity.RaceEntity;

@Repository
public interface DriverRepository extends JpaRepository<DriverEntity, UUID> {
    @Query("""
            SELECT d FROM DriverEntity d
            JOIN FETCH d.race
            WHERE d.id = :id
            """)
    Optional<DriverEntity> findByIdWithDetails(@Param("id") UUID id);

    @Query(value = """
                    SELECT d
                    FROM DriverEntity d
                    JOIN FETCH d.race
                    """,
            countQuery = """
                    SELECT count(d)
                    FROM DriverEntity d
                    """)
    Page<DriverEntity> findAllWithDetails(Pageable pageable);
    Page<DriverEntity> findByRaceOrderByFullName(RaceEntity race, Pageable pageable);

    @Modifying
    @Query("DELETE FROM DriverEntity d WHERE d.race.raceId = :raceId")
    void deleteAllByRaceId(@Param("raceId") UUID raceId);

    @Modifying
    @Query("DELETE FROM DriverEntity d WHERE d.id = :id")
    void deleteDriverById(@Param("id") UUID id);
}
