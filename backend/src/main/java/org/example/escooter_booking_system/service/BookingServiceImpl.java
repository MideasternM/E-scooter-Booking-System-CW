package org.example.escooter_booking_system.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.escooter_booking_system.dto.BookingRequestDTO;
import org.example.escooter_booking_system.dto.BookingDurationPopularityDTO;
import org.example.escooter_booking_system.dto.BookingExtensionRequestDTO;
import org.example.escooter_booking_system.dto.StaffBookingRequestDTO;
import org.example.escooter_booking_system.dto.BookingUserDiscountDTO;
import org.example.escooter_booking_system.model.Booking;
import org.example.escooter_booking_system.model.Scooter;
import org.example.escooter_booking_system.model.User;
import org.example.escooter_booking_system.repository.BookingRepository;
import org.example.escooter_booking_system.repository.ScooterRepository;
import org.example.escooter_booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScooterRepository scooterRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    @Lazy
    private BookingService self;

    @Override
    @Transactional
    public Booking createBooking(BookingRequestDTO bookingRequest) {
        Long userId = bookingRequest.getUserId();
        Long scooterId = bookingRequest.getScooterId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Scooter scooter = scooterRepository.findById(scooterId)
                .orElseThrow(() -> new EntityNotFoundException("Scooter not found with id: " + scooterId));

        if (!scooter.getAvailable()) {
            throw new IllegalStateException("Scooter with id: " + scooterId + " is not available for booking.");
        }

        // Check if user has a discount based on weekly usage
        boolean hasDiscount = checkWeeklyUsageDiscount(userId);

        Booking newBooking = new Booking();

        newBooking.setUser(user);
        newBooking.setScooter(scooter);

        Instant reqStartTime = bookingRequest.getStartTime();
        Instant reqEndTime = bookingRequest.getEndTime();

        if (reqStartTime != null) {
            newBooking.setStartTime(Timestamp.from(reqStartTime));
        } else {
            newBooking.setStartTime(Timestamp.from(Instant.now()));
        }
        if (reqEndTime != null) {
            newBooking.setEndTime(Timestamp.from(reqEndTime));
        }
        newBooking.setStatus("Active");
        newBooking.setSelectedDurationLabel(bookingRequest.getSelectedDurationLabel());

        // Set whether the booking has a discount
        newBooking.setHasDiscount(hasDiscount);

        scooter.setAvailable(false);
        scooterRepository.save(scooter);

        Booking savedBooking = bookingRepository.save(newBooking);
        logger.info("Booking created successfully with ID: {}", savedBooking.getId());

        try {
            emailService.sendBookingConfirmationToUser(savedBooking);
        } catch (Exception e) {
            logger.error("Failed to send confirmation email for booking ID {}: {}", savedBooking.getId(),
                    e.getMessage());
        }

        return savedBooking;
    }

    @Override
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @Override
    public Booking updateBooking(Long id, Booking bookingDetails) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking != null) {
            booking.setStartTime(bookingDetails.getStartTime());
            booking.setEndTime(bookingDetails.getEndTime());
            booking.setStatus(bookingDetails.getStatus());
            return bookingRepository.save(booking);
        }
        return null;
    }

    @Override
    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));

        if ("Active".equals(booking.getStatus())) {
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);

            Scooter scooter = booking.getScooter();
            if (scooter != null) {
                scooter.setAvailable(true);
                scooterRepository.save(scooter);
            } else {
                System.err.println("Warning: Scooter not found for cancelled booking ID: " + id);
            }
        } else {
            throw new IllegalStateException(
                    "Booking with id " + id + " cannot be cancelled in its current state: " + booking.getStatus());
        }
    }

    @Override
    @Transactional
    public Booking completeBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));

        if (!"Active".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException(
                    "Booking with id " + id + " cannot be completed in its current state: " + booking.getStatus());
        }

        Instant endTimeInstant = Instant.now();
        booking.setStatus("Unpaid");
        booking.setEndTime(Timestamp.from(endTimeInstant));

        Booking completedBooking = bookingRepository.save(booking);

        Scooter scooter = completedBooking.getScooter();
        if (scooter != null) {
            scooter.setAvailable(true);
            scooterRepository.save(scooter);
        } else {
            System.err.println("Warning: Scooter not found for completed booking ID: " + id);
        }

        return completedBooking;
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDurationPopularityDTO getBookingDurationPopularity() {
        List<Booking> allBookings = bookingRepository.findAll();

        BookingDurationPopularityDTO popularityDTO = new BookingDurationPopularityDTO();

        for (Booking booking : allBookings) {
            popularityDTO.incrementCount(booking.getSelectedDurationLabel());
        }

        ensureStandardDurationKeys(popularityDTO);

        return popularityDTO;
    }

    private void ensureStandardDurationKeys(BookingDurationPopularityDTO dto) {
        String[] standardLabels = { "1 Hour", "4 Hours", "1 Day", "1 Week", "Unknown" };
        for (String label : standardLabels) {
            dto.getDurationCounts().putIfAbsent(label, 0L);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void checkAndCompleteOverdueBookings() {
        Instant now = Instant.now();
        logger.info("Running scheduled check for overdue bookings at {}", now);

        List<Booking> activeBookings = bookingRepository.findByStatusIgnoreCase("Active");

        int completedCount = 0;
        for (Booking booking : activeBookings) {
            if (booking.getEndTime() != null && booking.getEndTime().toInstant().isBefore(now)) {
                logger.info("Found overdue booking ID: {}. Scheduled End Time: {}", booking.getId(),
                        booking.getEndTime());
                try {
                    self.completeBooking(booking.getId());
                    completedCount++;
                    logger.info("Successfully auto-completed booking ID: {}", booking.getId());
                } catch (Exception e) {
                    logger.error("Error auto-completing booking ID: {}. Reason: {}", booking.getId(), e.getMessage(),
                            e);
                }
            }
        }
        if (completedCount > 0) {
            logger.info("Scheduled task completed {} overdue bookings.", completedCount);
        }
    }

    @Override
    @Transactional
    public Booking extendBooking(Long bookingId, BookingExtensionRequestDTO extensionRequest) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        if (!"Active".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Booking with id " + bookingId + " is not active and cannot be extended.");
        }

        String newLabel = extensionRequest.getNewDurationLabel();
        long newDurationMinutes = getDurationMinutesFromLabel(newLabel);
        if (newDurationMinutes <= 0) {
            throw new IllegalArgumentException("Invalid new duration label provided: " + newLabel);
        }

        if (booking.getStartTime() == null) {
            throw new IllegalStateException("Booking start time is missing for booking ID: " + bookingId);
        }
        Instant startTimeInstant = booking.getStartTime().toInstant();

        long currentDurationMinutes = 0;
        if (booking.getEndTime() != null) {
            currentDurationMinutes = Duration.between(startTimeInstant, booking.getEndTime().toInstant()).toMinutes();
        }

        Instant newEndTimeInstant = startTimeInstant.plus(newDurationMinutes, ChronoUnit.MINUTES);

        booking.setEndTime(Timestamp.from(newEndTimeInstant));
        booking.setSelectedDurationLabel(newLabel);

        logger.info("Extended booking ID: {}. New Label: {}, New End Time: {}",
                booking.getId(), newLabel, booking.getEndTime());

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking createBookingForGuestByStaff(StaffBookingRequestDTO request) {
        Long scooterId = request.getScooterId();
        String guestEmail = request.getGuestEmail();
        String durationLabel = request.getSelectedDurationLabel();
        Instant startTime = request.getStartTime() != null ? request.getStartTime() : Instant.now();

        Scooter scooter = scooterRepository.findById(scooterId)
                .orElseThrow(() -> new EntityNotFoundException("Scooter not found with id: " + scooterId));

        if (!scooter.getAvailable()) {
            throw new IllegalStateException("Scooter with id: " + scooterId + " is not available for booking.");
        }

        Booking newBooking = new Booking();
        newBooking.setUser(null);
        newBooking.setScooter(scooter);
        newBooking.setStatus("Active");
        newBooking.setSelectedDurationLabel(durationLabel);
        newBooking.setStartTime(Timestamp.from(startTime));

        long durationMinutes = getDurationMinutesFromLabel(durationLabel);
        if (durationMinutes > 0) {
            Instant endTime = startTime.plus(durationMinutes, ChronoUnit.MINUTES);
            newBooking.setEndTime(Timestamp.from(endTime));
        } else {
            logger.warn("Could not determine end time for guest booking with label: {}", durationLabel);
        }

        scooter.setAvailable(false);
        scooterRepository.save(scooter);

        Booking savedBooking = bookingRepository.save(newBooking);
        logger.info("Guest booking created successfully by staff with ID: {}", savedBooking.getId());

        try {
            emailService.sendBookingConfirmationToGuest(savedBooking, guestEmail);
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to guest {} for booking ID {}: {}",
                    guestEmail, savedBooking.getId(), e.getMessage());
        }

        return savedBooking;
    }

    private long getDurationMinutesFromLabel(String label) {
        if (label == null)
            return 0;
        switch (label) {
            case "1 Hour":
                return 60;
            case "4 Hours":
                return 4 * 60;
            case "1 Day":
                return 24 * 60;
            case "1 Week":
                return 7 * 24 * 60;
            default:
                return 0;
        }
    }

    /**
     * Checks if the user has used the system for more than 8 hours in the past week
     * and is eligible for a 20% discount.
     * 
     * @param userId The user ID to check
     * @return true if the user is eligible for a discount, false otherwise
     */
    public boolean checkWeeklyUsageDiscount(Long userId) {
        return calculateWeeklyUsage(userId).isEligibleForDiscount();
    }

    /**
     * Calculates the weekly usage for a user and returns discount information.
     * 
     * @param userId The user ID to calculate usage for
     * @return BookingUserDiscountDTO containing usage and discount information
     */
    @Override
    public BookingUserDiscountDTO calculateWeeklyUsage(Long userId) {
        // Required hours for discount
        final double REQUIRED_HOURS = 8.0;

        // Get the timestamp from 7 days ago
        Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        Timestamp oneWeekAgoTimestamp = Timestamp.from(oneWeekAgo);

        // Get all completed bookings for this user in the past week
        List<Booking> recentBookings = bookingRepository.findCompletedBookingsByUserIdAfterDate(userId,
                oneWeekAgoTimestamp);

        // Calculate total duration in hours
        double totalHoursUsed = 0;
        for (Booking booking : recentBookings) {
            if (booking.getStartTime() != null && booking.getEndTime() != null) {
                // Calculate duration between start and end times
                long durationMillis = booking.getEndTime().getTime() - booking.getStartTime().getTime();
                double durationHours = durationMillis / (1000.0 * 60 * 60); // Convert to hours
                totalHoursUsed += durationHours;
            }
        }

        logger.info("User {} has used the system for {} hours in the past week", userId, totalHoursUsed);

        // Check if total usage is more than 8 hours
        boolean eligibleForDiscount = totalHoursUsed >= REQUIRED_HOURS;

        // Build and return the DTO
        return new BookingUserDiscountDTO(
                eligibleForDiscount,
                eligibleForDiscount ? 0.8 : 1.0, // 0.8 = 20% discount
                totalHoursUsed,
                REQUIRED_HOURS);
    }

    /**
     * Gets the discount rate (0.8 = 20% discount) if the user qualifies for one
     * 
     * @return 0.8 if user qualifies for discount, 1.0 otherwise
     */
    public double getDiscountRate(Long userId) {
        return calculateWeeklyUsage(userId).getDiscountRate();
    }

    @Override
    @Transactional
    public Booking updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }
}
