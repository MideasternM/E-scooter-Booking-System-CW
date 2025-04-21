package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.Booking;

/**
 * Service interface for sending emails.
 */
public interface EmailService {

    /**
     * Sends a simple text email.
     *
     * @param to      Recipient email address.
     * @param subject Email subject.
     * @param text    Email body content.
     */
    void sendSimpleMessage(String to, String subject, String text);

    /**
     * Sends a booking confirmation email to a registered user associated with the
     * booking.
     *
     * @param booking The booking details.
     */
    void sendBookingConfirmationToUser(Booking booking);

    /**
     * Sends a booking confirmation email to a guest (non-registered user).
     *
     * @param booking    The booking details.
     * @param guestEmail The email address of the guest.
     */
    void sendBookingConfirmationToGuest(Booking booking, String guestEmail);

    // Potentially add more methods for other email types (e.g., password reset,
    // issue updates)
}