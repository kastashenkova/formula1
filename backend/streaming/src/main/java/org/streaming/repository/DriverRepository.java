package org.streaming.repository;

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
    Page<DriverEntity> findByRaceOrderByFullName(RaceEntity race, Pageable pageable);

    @Modifying
    @Query("DELETE FROM DriverEntity d WHERE d.race.raceId = :raceId")
    void deleteAllByRaceId(@Param("raceId") UUID raceId);
}
