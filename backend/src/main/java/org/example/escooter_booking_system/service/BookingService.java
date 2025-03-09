package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.Booking;
import java.util.List;

public interface BookingService {
    Booking createBooking(Booking booking);

    List<Booking> getUserBookings(Long userId);

    Booking getBookingById(Long id);

    Booking updateBooking(Long id, Booking bookingDetails);

    void cancelBooking(Long id);
}
