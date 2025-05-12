package org.example.escooter_booking_system.repository;

import org.example.escooter_booking_system.model.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Date;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Override findAll to eagerly fetch booking and its user/scooter
    @Override
    @EntityGraph(attributePaths = { "booking", "booking.user",
            "booking.scooter" }, type = EntityGraph.EntityGraphType.FETCH)
    List<Payment> findAll();

    @EntityGraph(attributePaths = { "booking", "booking.user",
            "booking.scooter" }, type = EntityGraph.EntityGraphType.FETCH)
    List<Payment> findByBookingId(Long bookingId);

    List<Payment> findByStatus(String status);

    Payment findByTransactionId(String transactionId);

    boolean existsByBookingId(Long bookingId);

    // Add EntityGraph to ensure booking and its user/scooter are fetched correctly
    @EntityGraph(attributePaths = { "booking", "booking.user",
            "booking.scooter" }, type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT p FROM Payment p JOIN p.booking b WHERE p.type = :type AND p.status = :status AND p.completedAt BETWEEN :startDate AND :endDate")
    List<Payment> findCompletedRentalFeesBetweenDates(
            @Param("type") String type,
            @Param("status") String status,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);
}
