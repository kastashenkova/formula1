package org.streaming.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "drivers")
public class DriverEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    Long driverNumber;
    @Column(nullable = false)
    String fullName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_id", nullable = false)
    RaceEntity race;

    protected DriverEntity() {
    }

    public DriverEntity(Long id, Long driverNumber, String fullName, RaceEntity race) {
        this.id = id;
        this.driverNumber = driverNumber;
        this.fullName = fullName;
        this.race = race;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDriverNumber() {
        return driverNumber;
    }

    public void setDriverNumber(Long driverNumber) {
        this.driverNumber = driverNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public RaceEntity getRace() {
        return race;
    }

    public void setRace(RaceEntity race) {
        this.race = race;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DriverEntity that = (DriverEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
