package org.example.escooter_booking_system.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Scooter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String scooterCode;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private BigDecimal batteryLevel;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Boolean isAvailable = true;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScooterCode() {
        return scooterCode;
    }

    public void setScooterCode(String scooterCode) {
        this.scooterCode = scooterCode;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public BigDecimal getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(BigDecimal batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
}
