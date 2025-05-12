package org.example.escooter_booking_system.repository;

import org.example.escooter_booking_system.model.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = { "payment", "user", "scooter" }, type = EntityGraph.EntityGraphType.FETCH)
    List<Booking> findByUserId(Long userId);

    @EntityGraph(attributePaths = { "payment", "user", "scooter" }, type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<Booking> findById(Long id);

    // 重写 findAll 方法，使用 EntityGraph 确保使用 LEFT JOIN
    @EntityGraph(attributePaths = { "payment", "user", "scooter" }, type = EntityGraph.EntityGraphType.FETCH)
    @Override
    List<Booking> findAll();

    List<Booking> findByScooterId(Long scooterId);

    List<Booking> findByStatus(String status);

    @EntityGraph(attributePaths = { "payment", "user", "scooter" }, type = EntityGraph.EntityGraphType.FETCH)
    List<Booking> findByStatusIgnoreCase(String status);

    /**
     * Find all completed bookings for a user after a specific date.
     * Used for calculating weekly usage for discount eligibility.
     *
     * @param userId User ID to find bookings for
     * @param date   Date after which to find bookings
     * @return List of completed bookings for the user after the specified date
     */
    @Query("SELECT b FROM Booking b WHERE b.user IS NOT NULL AND b.user.id = :userId AND b.status = 'Completed' AND b.endTime >= :date")
    List<Booking> findCompletedBookingsByUserIdAfterDate(@Param("userId") Long userId, @Param("date") Timestamp date);
}
