package org.example.escooter_booking_system.repository;

import org.example.escooter_booking_system.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);

    List<Payment> findByStatus(String status);

    Payment findByTransactionId(String transactionId);
}
