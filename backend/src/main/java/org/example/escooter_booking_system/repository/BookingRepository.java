package org.example.escooter_booking_system.repository;

import org.example.escooter_booking_system.model.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = { "payment", "user", "scooter" })
    List<Booking> findByUserId(Long userId);

    List<Booking> findByScooterId(Long scooterId);

    List<Booking> findByStatus(String status);

    List<Booking> findByStatusIgnoreCase(String status);

    /**
     * Find all completed bookings for a user after a specific date.
     * Used for calculating weekly usage for discount eligibility.
     *
     * @param userId User ID to find bookings for
     * @param date   Date after which to find bookings
     * @return List of completed bookings for the user after the specified date
     */
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = 'Completed' AND b.endTime >= :date")
    List<Booking> findCompletedBookingsByUserIdAfterDate(@Param("userId") Long userId, @Param("date") Timestamp date);
}
