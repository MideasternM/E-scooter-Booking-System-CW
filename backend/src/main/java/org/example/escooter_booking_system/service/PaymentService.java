package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.Payment;
import org.example.escooter_booking_system.dto.WeeklyIncomeBreakdownDTO;
import org.example.escooter_booking_system.dto.DailyIncomeDTO;
import java.util.List;

public interface PaymentService {
    Payment processPayment(Payment payment);

    List<Payment> getPaymentsByBooking(Long bookingId);

    Payment getPaymentById(Long id);

    Payment updatePaymentStatus(Long id, String status);

    Payment createPaymentForBooking(Long bookingId);

    WeeklyIncomeBreakdownDTO getWeeklyIncomeBreakdown();

    DailyIncomeDTO getDailyIncomeForCurrentWeek();
}
