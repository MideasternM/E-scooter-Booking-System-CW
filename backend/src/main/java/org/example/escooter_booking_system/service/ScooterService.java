package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.Scooter;
import java.util.List;

public interface ScooterService {
    Scooter addScooter(Scooter scooter);

    List<Scooter> getAllScooters();

    List<Scooter> getAvailableScooters();

    Scooter getScooterById(Long id);

    Scooter updateScooter(Long id, Scooter scooterDetails);

    Scooter updateScooterStatus(Long id, String status);

    void deleteScooter(Long id);
}
