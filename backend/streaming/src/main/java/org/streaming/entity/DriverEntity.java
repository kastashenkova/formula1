package org.streaming.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "drivers")
public class DriverEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private Long driverNumber;
    @Column(nullable = false)
    private String fullName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_id", nullable = false)
    private RaceEntity race;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TelemetryPointEntity> telemetryPoints = new ArrayList<>();

    protected DriverEntity() {
    }

    public DriverEntity(Long driverNumber, String fullName) {
        this.driverNumber = driverNumber;
        this.fullName = fullName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public List<TelemetryPointEntity> getTelemetryPoints() {
        return telemetryPoints;
    }

    public void setTelemetryPoints(List<TelemetryPointEntity> telemetryPoints) {
        this.telemetryPoints = telemetryPoints;
    }

    public void addTelemetryPoint(TelemetryPointEntity telemetryPointEntity) {
        telemetryPoints.add(telemetryPointEntity);
        telemetryPointEntity.setDriver(this);
    }

    public void removeTelemetryPoint(TelemetryPointEntity telemetryPointEntity) {
        telemetryPoints.remove(telemetryPointEntity);
        telemetryPointEntity.setDriver(null);
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
