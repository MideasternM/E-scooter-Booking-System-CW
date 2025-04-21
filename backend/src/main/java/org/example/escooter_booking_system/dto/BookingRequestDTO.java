package org.example.escooter_booking_system.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public class BookingRequestDTO {
    private Long scooterId;
    private Long userId;
    private Instant startTime;
    private Instant endTime;
    private String selectedDurationLabel;

    // Getters and Setters

    public Long getScooterId() {
        return scooterId;
    }

    public void setScooterId(Long scooterId) {
        this.scooterId = scooterId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public String getSelectedDurationLabel() {
        return selectedDurationLabel;
    }

    public void setSelectedDurationLabel(String selectedDurationLabel) {
        this.selectedDurationLabel = selectedDurationLabel;
    }
}