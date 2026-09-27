package org.streaming.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "races")
public class RaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID raceId;

    @Column(nullable = false, length = 150, unique = true)
    private String raceName;

    @Column(nullable = false)
    private LocalDateTime raceDate;

    @OneToMany(mappedBy = "race", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DriverEntity> drivers = new ArrayList<>();

    protected RaceEntity() {
    }

    public RaceEntity(String raceName, LocalDateTime raceDate) {
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

    public List<DriverEntity> getDrivers() {
        return drivers;
    }

    public void setDrivers(List<DriverEntity> drivers) {
        this.drivers = drivers;
    }

    public void addDriver(DriverEntity driver) {
        drivers.add(driver);
        driver.setRace(this);
    }

    public void removeDriver(DriverEntity driver) {
        drivers.remove(driver);
        driver.setRace(null);
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
