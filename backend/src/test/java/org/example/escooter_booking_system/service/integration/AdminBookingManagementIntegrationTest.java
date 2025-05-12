package org.example.escooter_booking_system.service.integration;

import org.example.escooter_booking_system.config.TestApplication;
import org.example.escooter_booking_system.model.Booking;
import org.example.escooter_booking_system.model.Payment;
import org.example.escooter_booking_system.model.Scooter;
import org.example.escooter_booking_system.model.User;
import org.example.escooter_booking_system.repository.BookingRepository;
import org.example.escooter_booking_system.repository.ScooterRepository;
import org.example.escooter_booking_system.repository.UserRepository;
import org.example.escooter_booking_system.service.BookingService;
import org.example.escooter_booking_system.service.ScooterService;
import org.example.escooter_booking_system.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb_admin_booking;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.show-sql=true",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.h2.console.enabled=true",
    "spring.h2.console.path=/h2-console-admin-booking"
})
@Transactional
public class AdminBookingManagementIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ScooterService scooterService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScooterRepository scooterRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User createAndSaveUser(String username, String email, String phone) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setName("Test User " + username);
        user.setPhoneNumber(phone);
        return userRepository.save(user);
    }

    private Scooter createAndSaveScooter(String scooterCode, boolean isAvailable) {
        Scooter scooter = new Scooter();
        scooter.setScooterCode(scooterCode);
        scooter.setModel("TestModel");
        scooter.setBatteryLevel(new BigDecimal("100"));
        scooter.setLocation("TestLocation");
        scooter.setAvailable(isAvailable);
        scooter.setLastMaintenanceDate(new Date());
        return scooterRepository.save(scooter);
    }

    private Booking createAndSaveBooking(User user, Scooter scooter, Instant startTimeInstant, Instant endTimeInstant, String bookingStatus) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setScooter(scooter);
        booking.setStartTime(Date.from(startTimeInstant));
        booking.setEndTime(Date.from(endTimeInstant));
        booking.setStatus(bookingStatus);
        booking.setSelectedDurationLabel("1 Hour");

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(new BigDecimal("10.00"));
        payment.setPaymentMethod("CARD");
        payment.setTransactionId("TRANS-" + System.currentTimeMillis());
        payment.setType("RENTAL_FEE");
        payment.setStatus("Paid");
        
        booking.setPayment(payment);

        return bookingRepository.save(booking);
    }

    @Test
    void testSystemAutomaticallyCompletesOverdueBookings() {
        // 1. Prepare data
        User user1 = createAndSaveUser("overdueUser", "overdue@example.com", "1111111111");

        Scooter scooter1_overdue = createAndSaveScooter("SCOOTER_OVERDUE1", false); // Initially unavailable
        Scooter scooter2_active = createAndSaveScooter("SCOOTER_ACTIVE1", false);   // Initially unavailable
        Scooter scooter3_overdue_already_completed = createAndSaveScooter("SCOOTER_OVERDUE_COMP", true); // Was booked, now available

        Instant now = Instant.now();
        Booking overdueBooking1 = createAndSaveBooking(user1, scooter1_overdue,
                now.minus(2, ChronoUnit.HOURS), now.minus(1, ChronoUnit.HOURS), "Active");

        Booking activeBooking1 = createAndSaveBooking(user1, scooter2_active,
                now.minus(30, ChronoUnit.MINUTES), now.plus(30, ChronoUnit.MINUTES), "Active");
        
        Booking alreadyCompletedBooking = createAndSaveBooking(user1, scooter3_overdue_already_completed,
                now.minus(3, ChronoUnit.HOURS), now.minus(2, ChronoUnit.HOURS), "Completed");
        // Ensure scooter for already completed booking is available as it should have been released
        scooter3_overdue_already_completed.setAvailable(true);
        scooterRepository.save(scooter3_overdue_already_completed);

        // 2. Execute action
        bookingService.checkAndCompleteOverdueBookings();

        // 3. Verify results
        Booking fetchedOverdueBooking1 = bookingRepository.findById(overdueBooking1.getId()).orElse(null);
        assertNotNull(fetchedOverdueBooking1);
        assertEquals("Completed", fetchedOverdueBooking1.getStatus(), "Overdue booking should be completed.");
        Scooter fetchedScooter1 = scooterRepository.findById(scooter1_overdue.getId()).orElse(null);
        assertNotNull(fetchedScooter1);
        assertTrue(fetchedScooter1.getAvailable(), "Scooter from overdue booking should become available.");

        Booking fetchedActiveBooking1 = bookingRepository.findById(activeBooking1.getId()).orElse(null);
        assertNotNull(fetchedActiveBooking1);
        assertEquals("Active", fetchedActiveBooking1.getStatus(), "Active booking should remain active.");
        Scooter fetchedScooter2 = scooterRepository.findById(scooter2_active.getId()).orElse(null);
        assertNotNull(fetchedScooter2);
        assertFalse(fetchedScooter2.getAvailable(), "Scooter from active booking should remain unavailable.");

        Booking fetchedAlreadyCompletedBooking = bookingRepository.findById(alreadyCompletedBooking.getId()).orElse(null);
        assertNotNull(fetchedAlreadyCompletedBooking);
        assertEquals("Completed", fetchedAlreadyCompletedBooking.getStatus(), "Already completed booking should remain completed.");
        Scooter fetchedScooter3 = scooterRepository.findById(scooter3_overdue_already_completed.getId()).orElse(null);
        assertNotNull(fetchedScooter3);
        assertTrue(fetchedScooter3.getAvailable(), "Scooter from already completed booking should remain available.");
    }

    @Test
    void testAdminGetAllBookings_WhenBookingsExist() {
        User user1 = createAndSaveUser("userA", "usera@example.com", "2222222222");
        User user2 = createAndSaveUser("userB", "userb@example.com", "3333333333");
        Scooter scooter1 = createAndSaveScooter("SC001", false);
        Scooter scooter2 = createAndSaveScooter("SC002", false);

        createAndSaveBooking(user1, scooter1, Instant.now().minus(1, ChronoUnit.HOURS), Instant.now(), "Completed");
        createAndSaveBooking(user2, scooter2, Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS), "Active");

        List<Booking> allBookings = bookingService.getAllBookings();
        assertNotNull(allBookings);
        assertEquals(2, allBookings.size(), "Should retrieve all created bookings.");
    }

    @Test
    void testAdminGetAllBookings_WhenNoBookingsExist() {
        // userRepository.deleteAll(); // Not strictly needed due to @Transactional rollback but good for clarity if this were a non-transactional setup
        // scooterRepository.deleteAll();
        // bookingRepository.deleteAll();
        List<Booking> allBookings = bookingService.getAllBookings();
        assertNotNull(allBookings);
        assertTrue(allBookings.isEmpty(), "Should return an empty list when no bookings exist.");
    }

    @Test
    void testGetBookingDurationPopularity_WithVariousDurations() {
        User user1 = createAndSaveUser("userPopularity", "userpop@example.com", "4444444444");
        Scooter scooter1 = createAndSaveScooter("SCPOP01", true);
        Scooter scooter2 = createAndSaveScooter("SCPOP02", true);
        Scooter scooter3 = createAndSaveScooter("SCPOP03", true);
        Scooter scooter4 = createAndSaveScooter("SCPOP04", true);
        Scooter scooter5 = createAndSaveScooter("SCPOP05", true);

        Booking b1 = new Booking();
        b1.setUser(user1); b1.setScooter(scooter1); b1.setStartTime(Date.from(Instant.now())); b1.setEndTime(Date.from(Instant.now().plusSeconds(3600))); b1.setStatus("Active"); b1.setSelectedDurationLabel("1 Hour");
        Payment p1 = new Payment(); p1.setBooking(b1); p1.setAmount(BigDecimal.TEN); p1.setStatus("Paid"); p1.setPaymentMethod("Card"); p1.setType("RENTAL_FEE"); p1.setTransactionId("T1"); b1.setPayment(p1);
        bookingRepository.save(b1);
        scooter1.setAvailable(false); scooterRepository.save(scooter1);

        Booking b2 = new Booking();
        b2.setUser(user1); b2.setScooter(scooter2); b2.setStartTime(Date.from(Instant.now())); b2.setEndTime(Date.from(Instant.now().plusSeconds(3600*4))); b2.setStatus("Active"); b2.setSelectedDurationLabel("4 Hours");
        Payment p2 = new Payment(); p2.setBooking(b2); p2.setAmount(BigDecimal.TEN); p2.setStatus("Paid"); p2.setPaymentMethod("Card"); p2.setType("RENTAL_FEE"); p2.setTransactionId("T2"); b2.setPayment(p2);
        bookingRepository.save(b2);
        scooter2.setAvailable(false); scooterRepository.save(scooter2);

        Booking b3 = new Booking(); // Another "1 Hour"
        b3.setUser(user1); b3.setScooter(scooter3); b3.setStartTime(Date.from(Instant.now())); b3.setEndTime(Date.from(Instant.now().plusSeconds(3600))); b3.setStatus("Completed"); b3.setSelectedDurationLabel("1 Hour");
        Payment p3 = new Payment(); p3.setBooking(b3); p3.setAmount(BigDecimal.TEN); p3.setStatus("Paid"); p3.setPaymentMethod("Card"); p3.setType("RENTAL_FEE"); p3.setTransactionId("T3"); b3.setPayment(p3);
        bookingRepository.save(b3);
        // Scooter for completed booking should be available, but let's assume it was handled

        Booking b4 = new Booking(); // Custom or null label, should go to "Unknown"
        b4.setUser(user1); b4.setScooter(scooter4); b4.setStartTime(Date.from(Instant.now())); b4.setEndTime(Date.from(Instant.now().plusSeconds(1800))); b4.setStatus("Active"); b4.setSelectedDurationLabel(null); // Null label
        Payment p4 = new Payment(); p4.setBooking(b4); p4.setAmount(BigDecimal.TEN); p4.setStatus("Paid"); p4.setPaymentMethod("Card"); p4.setType("RENTAL_FEE"); p4.setTransactionId("T4"); b4.setPayment(p4);
        bookingRepository.save(b4);
        scooter4.setAvailable(false); scooterRepository.save(scooter4);

        Booking b5 = new Booking(); // Custom or empty label, should go to "Unknown"
        b5.setUser(user1); b5.setScooter(scooter5); b5.setStartTime(Date.from(Instant.now())); b5.setEndTime(Date.from(Instant.now().plusSeconds(7200))); b5.setStatus("Active"); b5.setSelectedDurationLabel(""); // Empty label
        Payment p5 = new Payment(); p5.setBooking(b5); p5.setAmount(BigDecimal.TEN); p5.setStatus("Paid"); p5.setPaymentMethod("Card"); p5.setType("RENTAL_FEE"); p5.setTransactionId("T5"); b5.setPayment(p5);
        bookingRepository.save(b5);
        scooter5.setAvailable(false); scooterRepository.save(scooter5);


        org.example.escooter_booking_system.dto.BookingDurationPopularityDTO popularityDTO = bookingService.getBookingDurationPopularity();
        assertNotNull(popularityDTO);
        assertNotNull(popularityDTO.getDurationCounts());
        assertEquals(2, popularityDTO.getDurationCounts().getOrDefault("1 Hour", 0L), "Should have 2 '1 Hour' bookings.");
        assertEquals(1, popularityDTO.getDurationCounts().getOrDefault("4 Hours", 0L), "Should have 1 '4 Hours' booking.");
        assertEquals(2, popularityDTO.getDurationCounts().getOrDefault("Unknown", 0L), "Should have 2 'Unknown' bookings (null and empty label).");
        // Check other standard labels are present with 0 count if ensureStandardDurationKeys populates them
        assertEquals(0, popularityDTO.getDurationCounts().getOrDefault("1 Day", 0L));
        assertEquals(0, popularityDTO.getDurationCounts().getOrDefault("1 Week", 0L));
    }

    @Test
    void testGetBookingDurationPopularity_WhenNoBookingsExist() {
        org.example.escooter_booking_system.dto.BookingDurationPopularityDTO popularityDTO = bookingService.getBookingDurationPopularity();
        assertNotNull(popularityDTO);
        assertNotNull(popularityDTO.getDurationCounts());
        // Depending on ensureStandardDurationKeys, map might not be empty but counts for specific durations would be 0.
        // Let's assume standard keys are always present due to ensureStandardDurationKeys
        assertEquals(0, popularityDTO.getDurationCounts().getOrDefault("1 Hour", 0L));
        assertEquals(0, popularityDTO.getDurationCounts().getOrDefault("4 Hours", 0L));
        assertEquals(0, popularityDTO.getDurationCounts().getOrDefault("1 Day", 0L));
        assertEquals(0, popularityDTO.getDurationCounts().getOrDefault("1 Week", 0L));
        assertEquals(0, popularityDTO.getDurationCounts().getOrDefault("Unknown", 0L));
        // A more general check if the map should only contain zeros for standard keys:
        assertTrue(popularityDTO.getDurationCounts().values().stream().allMatch(count -> count == 0), "All duration counts should be 0 when no bookings exist.");
    }
} 