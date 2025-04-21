package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.Booking;
import org.example.escooter_booking_system.dto.BookingRequestDTO;
import org.example.escooter_booking_system.dto.BookingDurationPopularityDTO;
import org.example.escooter_booking_system.dto.BookingExtensionRequestDTO;
import org.example.escooter_booking_system.dto.StaffBookingRequestDTO;
import java.util.List;

public interface BookingService {
    Booking createBooking(BookingRequestDTO bookingRequest);

    List<Booking> getUserBookings(Long userId);

    Booking getBookingById(Long id);

    Booking updateBooking(Long id, Booking bookingDetails);

    void cancelBooking(Long id);

    Booking completeBooking(Long id);

    List<Booking> getAllBookings();

    BookingDurationPopularityDTO getBookingDurationPopularity();

    Booking extendBooking(Long bookingId, BookingExtensionRequestDTO extensionRequest);

    /**
     * Creates a booking for a guest user, initiated by a staff member.
     *
     * @param request The DTO containing booking details and guest email.
     * @return The created booking.
     */
    Booking createBookingForGuestByStaff(StaffBookingRequestDTO request);
}
