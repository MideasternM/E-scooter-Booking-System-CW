package org.example.escooter_booking_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

/**
 * DTO for staff creating a booking for a guest user.
 */
public class StaffBookingRequestDTO {

    @NotNull(message = "Scooter ID cannot be null")
    @Positive(message = "Scooter ID must be positive")
    private Long scooterId;

    // Duration is typically selected via label, start/end calculated based on it
    @NotBlank(message = "Duration label cannot be blank")
    private String selectedDurationLabel;

    @NotBlank(message = "Guest email cannot be blank")
    @Email(message = "Guest email should be valid")
    private String guestEmail;

    // Optional: Start time (defaults to now if null)
    private Instant startTime;

    // Optional: Guest Name (for email personalization)
    private String guestName;

    // Getters and Setters

    public Long getScooterId() {
        return scooterId;
    }

    public void setScooterId(Long scooterId) {
        this.scooterId = scooterId;
    }

    public String getSelectedDurationLabel() {
        return selectedDurationLabel;
    }

    public void setSelectedDurationLabel(String selectedDurationLabel) {
        this.selectedDurationLabel = selectedDurationLabel;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

}