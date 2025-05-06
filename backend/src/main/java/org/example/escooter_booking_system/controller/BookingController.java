package org.example.escooter_booking_system.controller;

import org.example.escooter_booking_system.model.Booking;
import org.example.escooter_booking_system.service.BookingService;
import org.example.escooter_booking_system.dto.BookingRequestDTO;
import org.example.escooter_booking_system.dto.BookingExtensionRequestDTO;
import org.example.escooter_booking_system.dto.StaffBookingRequestDTO;
import org.example.escooter_booking_system.dto.BookingUserDiscountDTO;
import jakarta.validation.Valid;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:8080")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequestDTO bookingRequest) {
        Booking createdBooking = bookingService.createBooking(bookingRequest);
        return ResponseEntity.ok(createdBooking);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getUserBookings(@PathVariable Long userId) {
        List<Booking> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        if (booking != null) {
            return ResponseEntity.ok(booking);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id,
            @RequestBody Booking bookingDetails) {
        Booking updatedBooking = bookingService.updateBooking(id, bookingDetails);
        if (updatedBooking != null) {
            return ResponseEntity.ok(updatedBooking);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Booking> completeBooking(@PathVariable Long id) {
        Booking completedBooking = bookingService.completeBooking(id);
        return ResponseEntity.ok(completedBooking);
    }

    @PutMapping("/{id}/extend")
    public ResponseEntity<Booking> extendBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingExtensionRequestDTO extensionRequest) {
        try {
            Booking extendedBooking = bookingService.extendBooking(id, extensionRequest);
            return ResponseEntity.ok(extendedBooking);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/staff/guest")
    public ResponseEntity<Booking> createGuestBookingByStaff(
            @Valid @RequestBody StaffBookingRequestDTO request) {
        try {
            Booking createdBooking = bookingService.createBookingForGuestByStaff(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found during guest booking creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalStateException e) {
            logger.error("Illegal state during guest booking creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument during guest booking creation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Unexpected error during guest booking creation by staff", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}/discount")
    public ResponseEntity<BookingUserDiscountDTO> getUserDiscountEligibility(@PathVariable Long userId) {
        try {
            BookingUserDiscountDTO discountInfo = bookingService.calculateWeeklyUsage(userId);
            return ResponseEntity.ok(discountInfo);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error checking discount eligibility for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
