package org.example.escooter_booking_system.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.escooter_booking_system.model.Booking;
import org.example.escooter_booking_system.model.Payment;
import org.example.escooter_booking_system.repository.BookingRepository;
import org.example.escooter_booking_system.repository.PaymentRepository;
import org.example.escooter_booking_system.dto.WeeklyIncomeBreakdownDTO;
import org.example.escooter_booking_system.dto.DailyIncomeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // 假设的费率：每分钟 0.15
    private static final BigDecimal RATE_PER_MINUTE = new BigDecimal("0.15");

    @Override
    @Transactional
    public Payment createPaymentForBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // 检查状态是否为 "Completed"
        if (!"Completed".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Booking " + bookingId + " is not completed yet.");
        }

        // 检查是否已存在支付记录 (避免重复创建)
        if (paymentRepository.existsByBookingId(bookingId)) {
            throw new IllegalStateException("Payment already exists for booking " + bookingId);
        }

        // 检查 startTime 和 endTime 是否存在
        if (booking.getStartTime() == null || booking.getEndTime() == null) {
            throw new IllegalStateException(
                    "Cannot calculate payment for booking ID " + bookingId + " due to missing start or end time.");
        }

        // 计算时长
        Instant startTimeInstant = booking.getStartTime().toInstant();
        Instant endTimeInstant = booking.getEndTime().toInstant();
        long durationMinutes = Duration.between(startTimeInstant, endTimeInstant).toMinutes();
        durationMinutes = Math.max(1, durationMinutes); // 最少按1分钟计费

        // 计算金额
        BigDecimal amount = RATE_PER_MINUTE.multiply(new BigDecimal(durationMinutes));

        // 创建 Payment 对象
        Instant now = Instant.now();
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentMethod("User");
        payment.setStatus("COMPLETED");
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setCreatedAt(Timestamp.from(now));
        payment.setCompletedAt(Timestamp.from(now));
        payment.setType("RENTAL_FEE");

        return paymentRepository.save(payment);
    }

    @Override
    public Payment processPayment(Payment payment) {
        payment.setStatus("COMPLETED");
        return paymentRepository.save(payment);
    }

    @Override
    public List<Payment> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    @Override
    public Payment updatePaymentStatus(Long id, String status) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment != null) {
            payment.setStatus(status);
            return paymentRepository.save(payment);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyIncomeBreakdownDTO getWeeklyIncomeBreakdown() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            cal.add(Calendar.DATE, 1);
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date weekStartDate = cal.getTime();

        cal.add(Calendar.DATE, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date weekEndDate = cal.getTime();

        List<Payment> weeklyPayments = paymentRepository.findCompletedRentalFeesBetweenDates(
                "RENTAL_FEE",
                "COMPLETED",
                weekStartDate,
                weekEndDate);

        WeeklyIncomeBreakdownDTO breakdown = new WeeklyIncomeBreakdownDTO();

        for (Payment payment : weeklyPayments) {
            Booking booking = payment.getBooking();
            String categoryLabel = "Other"; // Default category

            if (booking != null && booking.getSelectedDurationLabel() != null
                    && !booking.getSelectedDurationLabel().trim().isEmpty()) {
                categoryLabel = booking.getSelectedDurationLabel();
            }
            breakdown.addIncome(categoryLabel, payment.getAmount());
        }

        return breakdown;
    }

    @Override
    @Transactional(readOnly = true)
    public DailyIncomeDTO getDailyIncomeForCurrentWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            cal.add(Calendar.DATE, 1);
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date weekStartDate = cal.getTime();

        cal.add(Calendar.DATE, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date weekEndDate = cal.getTime();

        List<Payment> weeklyPayments = paymentRepository.findCompletedRentalFeesBetweenDates(
                "RENTAL_FEE",
                "COMPLETED",
                weekStartDate,
                weekEndDate);

        DailyIncomeDTO dailyIncome = new DailyIncomeDTO();

        Calendar paymentCal = Calendar.getInstance();
        for (Payment payment : weeklyPayments) {
            if (payment.getCompletedAt() != null) {
                paymentCal.setTime(payment.getCompletedAt());
                int dayOfWeek = paymentCal.get(Calendar.DAY_OF_WEEK);
                dailyIncome.addIncome(dayOfWeek, payment.getAmount());
            }
        }

        return dailyIncome;
    }
}
