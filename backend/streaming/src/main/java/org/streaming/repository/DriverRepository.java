package org.streaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.streaming.dto.DriverResponseDto;
import org.streaming.entity.DriverEntity;
import org.streaming.entity.RaceEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<DriverEntity, UUID> {
    List<DriverEntity> findByRaceOrderByFullName(RaceEntity race);
}
