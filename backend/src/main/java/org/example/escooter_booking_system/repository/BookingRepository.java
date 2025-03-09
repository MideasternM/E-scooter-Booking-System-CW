package org.example.escooter_booking_system.repository;

import org.example.escooter_booking_system.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    List<Booking> findByScooterId(Long scooterId);

    List<Booking> findByStatus(String status);
}
