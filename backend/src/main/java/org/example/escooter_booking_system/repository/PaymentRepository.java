package org.example.escooter_booking_system.repository;

import org.example.escooter_booking_system.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Date;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);

    List<Payment> findByStatus(String status);

    Payment findByTransactionId(String transactionId);

    boolean existsByBookingId(Long bookingId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.booking b WHERE p.type = :type AND p.status = :status AND p.completedAt BETWEEN :startDate AND :endDate")
    List<Payment> findCompletedRentalFeesBetweenDates(
            @Param("type") String type,
            @Param("status") String status,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);
}
