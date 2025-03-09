package org.example.escooter_booking_system.repository;

import org.example.escooter_booking_system.model.Scooter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScooterRepository extends JpaRepository<Scooter, Long> {
    List<Scooter> findByLocation(String location);

    List<Scooter> findByIsAvailable(Boolean isAvailable);

    Scooter findByScooterCode(String scooterCode);
}
