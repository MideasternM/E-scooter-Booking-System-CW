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
        int maxRetries = 3;
        int attempts = 0;
        boolean sent = false;
        Exception lastException = null;

        // 验证发件人邮箱地址
        if (senderEmailAddress == null || senderEmailAddress.isEmpty() || !senderEmailAddress.contains("@")) {
            logger.error("Invalid sender email address configured: '{}'. Check your application.properties.",
                    senderEmailAddress);
            throw new IllegalStateException("Email service is not properly configured: Invalid sender email address");
        }

        // 验证收件人邮箱地址
        if (to == null || to.isEmpty() || !to.contains("@")) {
            logger.error("Invalid recipient email address: '{}'", to);
            throw new IllegalArgumentException("Invalid recipient email address: " + to);
        }

        while (attempts < maxRetries && !sent) {
            attempts++;
            try {
                logger.debug("Email attempt #{} - Sending email to: {}, Subject: {}", attempts, to, subject);

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(senderEmailAddress); // Set the sender address
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);

                emailSender.send(message);
                logger.info("Email sent successfully to {} after {} attempt(s)", to, attempts);
                sent = true;

            } catch (MailException exception) {
                lastException = exception;
                logger.warn("Attempt #{} - Failed to send email to {}: {}",
                        attempts, to, exception.getMessage());

                // 只有在还有重试机会的情况下等待
                if (attempts < maxRetries) {
                    try {
                        // 指数退避重试策略 - 等待时间随着尝试次数增加
                        Thread.sleep(1000 * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("Email retry sleep interrupted", ie);
                    }
                }
            } catch (Exception e) {
                // 捕获其他异常
                lastException = e;
                logger.error("Unexpected error while sending email to {}: {}", to, e.getMessage(), e);
                break; // 对于非邮件异常，直接中断重试
            }
        }

        // 所有重试都失败
        if (!sent && lastException != null) {
            logger.error("All attempts to send email to {} failed after {} tries. Last error: {}",
                    to, attempts, lastException.getMessage());
            // 记录堆栈跟踪
            lastException.printStackTrace();

            // 不抛出异常，而是返回，这样即使邮件发送失败，业务流程也能继续
            // throw new RuntimeException("Failed to send email after multiple attempts",
            // lastException);
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
        // First, check if booking is null, as that's a more fundamental problem.
        if (booking == null) {
            logger.error("Cannot send booking confirmation to guest: Booking object is null. Guest email was: {}",
                    guestEmail);
            return;
        }

        if (guestEmail == null || guestEmail.isEmpty()) {
            // Now it's safe to use booking.getId() because booking is confirmed not to be
            // null.
            logger.warn("Cannot send booking confirmation to guest for booking ID {}: Guest email is missing",
                    booking.getId());
            return;
        }

        try {
            logger.info("Preparing to send booking confirmation email to guest: {}", guestEmail);

            if (booking.getScooter() == null) {
                // Changed to warn as buildBookingConfirmationText handles null scooter
                // gracefully.
                logger.warn("Booking {} has null scooter when sending confirmation to guest: {}",
                        booking.getId(), guestEmail);
            }

            String subject = "Your E-Scooter Booking Confirmation - #" + booking.getId();
            // For guests, we don't have a username, so use a generic greeting
            String text = buildBookingConfirmationText(booking, "Guest");

            logger.debug("Sending guest booking confirmation email - Subject: {}, Content: {}", subject, text);
            sendSimpleMessage(guestEmail, subject, text);

            logger.info("Successfully sent booking confirmation email to guest: {}", guestEmail);
        } catch (Exception e) {
            logger.error("Failed to send booking confirmation to guest email {}: {}",
                    guestEmail, e.getMessage(), e);
            // 记录完整的堆栈跟踪
            e.printStackTrace();
        }
    }

    /**
     * Helper method to build the text content for the booking confirmation email.
     */
    private String buildBookingConfirmationText(Booking booking, String recipientName) {
        try {
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
            } else {
                sb.append("- Scooter: Information not available\n");
                logger.warn("Booking {} has no associated scooter while building email text", booking.getId());
            }
            if (booking.getStartTime() != null) {
                sb.append("- Start Time: ").append(formatter.format(booking.getStartTime().toInstant())).append("\n");
            } else {
                sb.append("- Start Time: Not set\n");
            }
            if (booking.getEndTime() != null) {
                sb.append("- Scheduled End Time: ").append(formatter.format(booking.getEndTime().toInstant()))
                        .append("\n");
            } else {
                sb.append("- Scheduled End Time: Not set\n");
            }
            if (booking.getSelectedDurationLabel() != null) {
                sb.append("- Selected Duration: ").append(booking.getSelectedDurationLabel()).append("\n");
            } else {
                sb.append("- Selected Duration: Not specified\n");
            }
            sb.append("\nStatus: ").append(booking.getStatus());
            sb.append("\n\nWe hope you enjoy your ride!\n");
            sb.append("\nBest regards,\nThe E-Scooter Booking Team");

            return sb.toString();
        } catch (Exception e) {
            logger.error("Error building confirmation email text: {}", e.getMessage(), e);
            e.printStackTrace();
            return "Thank you for your booking! (Error occurred while generating detailed message)";
        }
    }
}