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
import java.math.RoundingMode;
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
import java.util.concurrent.TimeUnit;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private BookingService bookingService;

    @Override
    @Transactional
    public Payment createPaymentForBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == null || !booking.getStatus().equalsIgnoreCase("Unpaid")) {
            throw new IllegalStateException("Payment can only be created for unpaid bookings.");
        }

        if (paymentRepository.existsByBookingId(bookingId)) {
            throw new IllegalStateException("Payment already exists for booking: " + bookingId);
        }

        // Calculate the FINAL amount to be paid (already includes discount logic)
        BigDecimal finalAmount = calculateAmountForBooking(booking);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(finalAmount); // Set the final paid amount
        payment.setPaymentMethod("Credit Card");
        payment.setTransactionId(generateTransactionId());
        payment.setType("RENTAL_FEE");
        payment.setStatus("PENDING");
        payment.setCreatedAt(new Date());

        // Set discount flag and calculate the *value* of the discount if applied
        boolean hasDiscount = booking.getHasDiscount() != null && booking.getHasDiscount();
        payment.setHasDiscount(hasDiscount);

        if (hasDiscount) {
            // Calculate the original full price *before* discount
            long durationMillis = booking.getEndTime().getTime() - booking.getStartTime().getTime();
            if (durationMillis < 0)
                durationMillis = 0;
            long durationMinutes = (long) Math.ceil((double) durationMillis / (1000 * 60));
            if (durationMillis > 0 && durationMinutes == 0)
                durationMinutes = 1;

            String scooterModel = booking.getScooter() != null ? booking.getScooter().getModel() : null;
            BigDecimal baseRate;
            try {
                baseRate = appConfigService.getPriceForModel(scooterModel);
            } catch (Exception e) {
                System.err.println("Error fetching price for model '" + scooterModel + "' during discount calculation: "
                        + e.getMessage());
                baseRate = new BigDecimal("0.20"); // Fallback rate
            }
            if (baseRate == null)
                baseRate = new BigDecimal("0.20"); // Ensure not null

            BigDecimal fullAmount = baseRate.multiply(new BigDecimal(durationMinutes));

            // Calculate the discount amount (20% of full amount)
            BigDecimal discountRateValue = new BigDecimal("0.20"); // 20% discount rate
            BigDecimal calculatedDiscountAmount = fullAmount.multiply(discountRateValue)
                    .setScale(4, RoundingMode.HALF_UP); // Use same scale
            payment.setDiscountAmount(calculatedDiscountAmount); // Store the value of the discount
        } else {
            payment.setDiscountAmount(BigDecimal.ZERO); // Explicitly set to zero if no discount
        }

        return paymentRepository.save(payment);
    }

    @Override
    public Payment processPayment(Payment payment) {
        payment.setStatus("COMPLETED");
        payment.setCompletedAt(new Date());
        Payment savedPayment = paymentRepository.save(payment);

        // Update booking status to Completed
        Booking booking = savedPayment.getBooking();
        if (booking != null && "UNPAID".equalsIgnoreCase(booking.getStatus())) {
            bookingService.updateBookingStatus(booking.getId(), "Completed");
        }
        return savedPayment;
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

    // Calculate payment amount based on booking details
    private BigDecimal calculateAmountForBooking(Booking booking) {
        if (booking == null || booking.getStartTime() == null || booking.getEndTime() == null
                || booking.getScooter() == null) {
            return BigDecimal.ZERO;
        }

        // Calculate duration in minutes
        long durationMillis = booking.getEndTime().getTime() - booking.getStartTime().getTime();
        if (durationMillis < 0) {
            durationMillis = 0;
        }
        long durationMinutes = (long) Math.ceil((double) durationMillis / (1000 * 60));
        if (durationMillis > 0 && durationMinutes == 0) {
            durationMinutes = 1;
        }

        // Get the model-specific rate from AppConfigService
        String scooterModel = booking.getScooter().getModel();
        BigDecimal baseRate;
        try {
            baseRate = appConfigService.getPriceForModel(scooterModel);
        } catch (Exception e) {
            System.err.println(
                    "Error fetching price for model '" + scooterModel + "' from AppConfigService: " + e.getMessage());
            baseRate = new BigDecimal("0.20");
        }

        if (baseRate == null) {
            System.err.println("Base rate is null for model '" + scooterModel + "', using default 0.20");
            baseRate = new BigDecimal("0.20");
        }

        BigDecimal amount = baseRate.multiply(new BigDecimal(durationMinutes));

        // Apply discount if applicable
        if (booking.getHasDiscount() != null && booking.getHasDiscount()) {
            // Apply 20% discount
            BigDecimal discountRate = new BigDecimal("0.8");
            amount = amount.multiply(discountRate);
        }

        // Keep full precision for calculations, round only for final storage/display
        amount = amount.setScale(4, RoundingMode.HALF_UP);

        return amount;
    }

    /**
     * Generates a random transaction ID.
     * 
     * @return A random UUID string to use as transaction ID
     */
    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }
}
