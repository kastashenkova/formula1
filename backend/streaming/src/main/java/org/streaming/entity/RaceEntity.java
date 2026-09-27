package org.streaming.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "races")
public class RaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID raceId;

    @Column(nullable = false, length = 150)
    private String raceName;

    @Column(nullable = false)
    private LocalDateTime raceDate;

    protected RaceEntity() {
    }

    public RaceEntity(UUID raceId, String raceName, LocalDateTime raceDate) {
        this.raceId = raceId;
        this.raceName = raceName;
        this.raceDate = raceDate;
    }

    public UUID getRaceId() {
        return raceId;
    }

    public void setRaceId(UUID raceId) {
        this.raceId = raceId;
    }

    public String getRaceName() {
        return raceName;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public LocalDateTime getRaceDate() {
        return raceDate;
    }

    public void setRaceDate(LocalDateTime raceDate) {
        this.raceDate = raceDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RaceEntity that = (RaceEntity) o;
        return Objects.equals(raceId, that.raceId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(raceId);
    }
}
