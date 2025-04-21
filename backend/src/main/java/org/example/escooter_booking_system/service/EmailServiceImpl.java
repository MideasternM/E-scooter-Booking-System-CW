package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${sender.email.address}")
    private String senderEmailAddress;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmailAddress); // Set the sender address
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            logger.info("Email sent successfully to {}", to);
        } catch (MailException exception) {
            logger.error("Error sending email to {}: {}", to, exception.getMessage());
            // Depending on the policy, you might want to re-throw or handle differently
        }
    }

    @Override
    public void sendBookingConfirmationToUser(Booking booking) {
        if (booking.getUser() == null || booking.getUser().getEmail() == null) {
            logger.warn("Cannot send booking confirmation to user for booking ID {}: User or email is null",
                    booking.getId());
            return;
        }
        String to = booking.getUser().getEmail();
        String subject = "Booking Confirmation - #" + booking.getId();
        String text = buildBookingConfirmationText(booking, booking.getUser().getUsername()); // Use username if
                                                                                              // available

        sendSimpleMessage(to, subject, text);
    }

    @Override
    public void sendBookingConfirmationToGuest(Booking booking, String guestEmail) {
        if (guestEmail == null || guestEmail.isEmpty()) {
            logger.warn("Cannot send booking confirmation to guest for booking ID {}: Guest email is missing",
                    booking.getId());
            return;
        }
        String subject = "Your E-Scooter Booking Confirmation - #" + booking.getId();
        // For guests, we don't have a username, so use a generic greeting
        String text = buildBookingConfirmationText(booking, "Guest");

        sendSimpleMessage(guestEmail, subject, text);
    }

    /**
     * Helper method to build the text content for the booking confirmation email.
     */
    private String buildBookingConfirmationText(Booking booking, String recipientName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(recipientName).append(",\n\n");
        sb.append("Thank you for booking with us! Here are your booking details:\n\n");
        sb.append("- Booking ID: ").append(booking.getId()).append("\n");
        if (booking.getScooter() != null) {
            sb.append("- Scooter ID: ").append(booking.getScooter().getId()).append("\n");
            if (booking.getScooter().getModel() != null) {
                sb.append("- Scooter Model: ").append(booking.getScooter().getModel()).append("\n");
            }
        }
        if (booking.getStartTime() != null) {
            sb.append("- Start Time: ").append(formatter.format(booking.getStartTime().toInstant())).append("\n");
        }
        if (booking.getEndTime() != null) {
            sb.append("- Scheduled End Time: ").append(formatter.format(booking.getEndTime().toInstant())).append("\n");
        }
        if (booking.getSelectedDurationLabel() != null) {
            sb.append("- Selected Duration: ").append(booking.getSelectedDurationLabel()).append("\n");
        }
        sb.append("\nStatus: ").append(booking.getStatus());
        sb.append("\n\nWe hope you enjoy your ride!\n");
        sb.append("\nBest regards,\nThe E-Scooter Booking Team");

        return sb.toString();
    }
}