package org.example.escooter_booking_system.service.integration;

import org.example.escooter_booking_system.dto.BookingRequestDTO;
import org.example.escooter_booking_system.model.Booking;
import org.example.escooter_booking_system.model.Scooter;
import org.example.escooter_booking_system.model.User;
import org.example.escooter_booking_system.repository.UserRepository;
import org.example.escooter_booking_system.service.BookingService;
import org.example.escooter_booking_system.service.ScooterService;
import org.example.escooter_booking_system.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.example.escooter_booking_system.config.TestApplication;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.show-sql=true",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.h2.console.enabled=true",
    "spring.h2.console.path=/h2-console"
})
@Transactional
public class UserBookingIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ScooterService scooterService;

    @Autowired
    private BookingService bookingService;

    // It's often useful to have direct access to repositories in integration tests
    // for setup or verification, though using services is also fine.
    @Autowired
    private UserRepository userRepository;


    private User testUser;
    private Scooter testScooter;

    @BeforeEach
    void setUp() {
        // It's good practice to ensure a clean state or set up common entities here.
        // However, with @Transactional on each test method, the database is rolled back,
        // so explicit cleanup in @AfterEach might not be strictly necessary for H2.
        // For now, we'll create fresh entities for each test within the test methods
        // to ensure test independence, especially if not using @Transactional per method.
    }

    @Test
    @Transactional // Ensures the test runs in a transaction and rolls back automatically
    void testUserRegistrationAndFirstBooking() {
        // 1. Prepare data: Create a scooter entity and save
        Scooter scooter = new Scooter();
        scooter.setScooterCode("INTEG_S001");
        scooter.setModel("Model X");
        scooter.setBatteryLevel(new java.math.BigDecimal("90"));
        scooter.setLocation("Test Location Central");
        scooter.setAvailable(true);
        scooter.setLastMaintenanceDate(new Date());
        Scooter savedScooter = scooterService.addScooter(scooter);
        assertNotNull(savedScooter.getId(), "Scooter ID should not be null after saving.");

        // 2. User registration
        User user = new User();
        user.setUsername("integUser1");
        user.setPassword("password123");
        user.setEmail("integUser1@example.com"); // Unique email
        user.setName("Integration Test User One");
        user.setPhoneNumber("1234567890");
        
        User registeredUser = userService.registerUser(user);
        assertNotNull(registeredUser.getId(), "User ID should not be null after registration.");
        assertEquals("integUser1", registeredUser.getUsername(), "Username should match.");

        // 3. Create booking request
        BookingRequestDTO bookingRequest = new BookingRequestDTO();
        bookingRequest.setUserId(registeredUser.getId());
        bookingRequest.setScooterId(savedScooter.getId());
        bookingRequest.setStartTime(Instant.now());
        bookingRequest.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));
        bookingRequest.setSelectedDurationLabel("1 Hour");

        // 4. User creates booking
        Booking createdBooking = bookingService.createBooking(bookingRequest);
        assertNotNull(createdBooking.getId(), "Booking ID should not be null after creation.");
        assertEquals(registeredUser.getId(), createdBooking.getUser().getId(), "User ID in booking should match.");
        assertEquals(savedScooter.getId(), createdBooking.getScooter().getId(), "Scooter ID in booking should match.");
        assertEquals("Active", createdBooking.getStatus(), "Booking status should be Active.");

        // 5. Verify database state (optional but recommended)
        User fetchedUser = userService.getUserById(registeredUser.getId());
        assertNotNull(fetchedUser, "Fetched user should not be null.");
        
        Booking fetchedBooking = bookingService.getBookingById(createdBooking.getId());
        assertNotNull(fetchedBooking, "Fetched booking should not be null.");
        assertEquals("Active", fetchedBooking.getStatus(), "Fetched booking status should be Active.");

        Scooter fetchedScooter = scooterService.getScooterById(savedScooter.getId());
        assertNotNull(fetchedScooter, "Fetched scooter should not be null.");
        // BookingServiceImpl.createBooking should make the scooter unavailable
        assertFalse(fetchedScooter.getAvailable(), "Scooter should be unavailable after booking.");
    }

    @Test
    @Transactional
    void testBookingAttemptWithUnavailableScooter() {
        // 1. Prepare User
        User user = new User();
        user.setUsername("integUser2");
        user.setPassword("password123");
        user.setEmail("integUser2@example.com"); // Unique email
        user.setName("Integration Test User Two");
        user.setPhoneNumber("1234567891"); // Added phone number
        User registeredUser = userService.registerUser(user);
        assertNotNull(registeredUser.getId());

        // 2. Prepare an unavailable scooter
        Scooter scooter = new Scooter();
        scooter.setScooterCode("INTEG_S002_UNAVAIL");
        scooter.setModel("Model Y");
        scooter.setBatteryLevel(new java.math.BigDecimal("50"));
        scooter.setLocation("Test Location Garage");
        scooter.setAvailable(false); // Explicitly set as unavailable
        scooter.setLastMaintenanceDate(new Date());
        Scooter savedScooter = scooterService.addScooter(scooter);
        assertNotNull(savedScooter.getId());

        // 3. Create booking request
        BookingRequestDTO bookingRequest = new BookingRequestDTO();
        bookingRequest.setUserId(registeredUser.getId());
        bookingRequest.setScooterId(savedScooter.getId());
        bookingRequest.setStartTime(Instant.now());
        bookingRequest.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));
        bookingRequest.setSelectedDurationLabel("1 Hour");

        // 4. Attempt to create booking and expect failure
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bookingService.createBooking(bookingRequest);
        });
        assertTrue(exception.getMessage().contains("is not available for booking"), "Exception message should indicate scooter is unavailable.");
    }
    
    @Test
    @Transactional
    void testUserRetrievesTheirBookings() {
        // 1. Register a user
        User user = new User();
        user.setUsername("integUser3");
        user.setPassword("password");
        user.setEmail("integUser3@example.com"); // Unique email
        user.setName("Integration Test User Three");
        user.setPhoneNumber("1234567892"); // Added phone number
        User registeredUser = userService.registerUser(user);
        assertNotNull(registeredUser.getId());

        // 2. Create a couple of available scooters
        Scooter scooter1 = new Scooter();
        scooter1.setScooterCode("INTEG_S003A");
        scooter1.setAvailable(true);
        scooter1.setModel("Model S1"); // Added model
        scooter1.setLocation("Location A"); // Added location
        scooter1.setBatteryLevel(new java.math.BigDecimal("85.5")); // Added battery level
        scooter1 = scooterService.addScooter(scooter1);

        Scooter scooter2 = new Scooter();
        scooter2.setScooterCode("INTEG_S003B");
        scooter2.setAvailable(true);
        scooter2.setModel("Model S2"); // Added model
        scooter2.setLocation("Location B"); // Added location
        scooter2.setBatteryLevel(new java.math.BigDecimal("92.0")); // Added battery level
        scooter2 = scooterService.addScooter(scooter2);

        // 3. Create two bookings for the user
        BookingRequestDTO bookingRequest1 = new BookingRequestDTO();
        bookingRequest1.setUserId(registeredUser.getId());
        bookingRequest1.setScooterId(scooter1.getId());
        bookingRequest1.setStartTime(Instant.now().minus(2, ChronoUnit.HOURS));
        bookingRequest1.setEndTime(Instant.now().minus(1, ChronoUnit.HOURS)); // Past booking
        bookingRequest1.setSelectedDurationLabel("1 Hour");
        bookingService.createBooking(bookingRequest1);
        // bookingService.completeBooking(booking1.getId()); // Let's assume it was completed

        BookingRequestDTO bookingRequest2 = new BookingRequestDTO();
        bookingRequest2.setUserId(registeredUser.getId());
        bookingRequest2.setScooterId(scooter2.getId());
        bookingRequest2.setStartTime(Instant.now());
        bookingRequest2.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS)); // Active booking
        bookingRequest2.setSelectedDurationLabel("1 Hour");
        bookingService.createBooking(bookingRequest2);

        // 4. Retrieve bookings for the user
        List<Booking> userBookings = bookingService.getUserBookings(registeredUser.getId());

        // 5. Assert results
        assertNotNull(userBookings);
        assertEquals(2, userBookings.size(), "User should have two bookings.");
        
        // Check if the bookings belong to the user (more robust check)
        assertTrue(userBookings.stream().allMatch(b -> b.getUser().getId().equals(registeredUser.getId())));
    }

    @Test
    @Transactional
    void testUserRegistration_DuplicateEmail_ShouldFail() {
        // 1. Register an initial user
        User user1 = new User();
        user1.setUsername("originalUser");
        user1.setPassword("password123");
        user1.setEmail("duplicate@example.com");
        user1.setName("Original User");
        user1.setPhoneNumber("1234567893"); // Added phone number
        userService.registerUser(user1);

        // 2. Attempt to register another user with the same email
        User user2 = new User();
        user2.setUsername("anotherUser");
        user2.setPassword("password456");
        user2.setEmail("duplicate@example.com"); // Same email
        user2.setName("Another User");
        user2.setPhoneNumber("1234567894"); // Added phone number

        // 3. Expect DataIntegrityViolationException or similar due to unique constraint
        //    The exact exception might depend on JPA provider and database specifics.
        //    For H2, it's often a DataIntegrityViolationException from Spring Data layer.
        assertThrows(DataIntegrityViolationException.class, () -> {
            userService.registerUser(user2);
        }, "Registering user with duplicate email should fail.");
    }

    @Test
    @Transactional
    void testUserRegistration_DuplicateUsername_ShouldFail() {
        // 1. Register an initial user
        User user1 = new User();
        user1.setUsername("duplicateUser"); // Username to be duplicated
        user1.setPassword("password123");
        user1.setEmail("userA@example.com");
        user1.setName("User A");
        user1.setPhoneNumber("1234567895"); // Added phone number
        userService.registerUser(user1);

        // 2. Attempt to register another user with the same username
        User user2 = new User();
        user2.setUsername("duplicateUser"); // Same username
        user2.setPassword("password456");
        user2.setEmail("userB@example.com");
        user2.setName("User B");
        user2.setPhoneNumber("1234567896"); // Added phone number
        
        assertThrows(DataIntegrityViolationException.class, () -> {
            userService.registerUser(user2);
        }, "Registering user with duplicate username should fail.");
    }

} 