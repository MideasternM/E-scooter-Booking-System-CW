package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.Scooter;
import java.util.List;

public interface ScooterService {
    Scooter addScooter(Scooter scooter);

    List<Scooter> getAllScooters();

    List<Scooter> getAvailableScooters();

    Scooter getScooterById(Long id);

    Scooter updateScooter(Long id, Scooter scooterDetails);

    void deleteScooter(Long id);
}
