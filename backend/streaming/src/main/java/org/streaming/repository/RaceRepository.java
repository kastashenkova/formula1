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
import org.streaming.entity.RaceEntity;

@Repository
public interface RaceRepository extends JpaRepository<RaceEntity, UUID> {

    @Query("SELECT r FROM RaceEntity r " +
            "LEFT JOIN FETCH r.drivers " +
            "WHERE r.raceId = :id")
    Optional<RaceEntity> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
            SELECT r
            FROM RaceEntity r
            """)
    Page<RaceEntity> findAllWithDetails(Pageable pageable);

    boolean existsBySessionKey(Integer sessionKey);

    @Modifying
    @Query("DELETE FROM RaceEntity r WHERE r.raceId = :raceId")
    void deleteRaceById(@Param("raceId") UUID raceId);
}
