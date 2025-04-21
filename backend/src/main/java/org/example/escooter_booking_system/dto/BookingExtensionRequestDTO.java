package org.example.escooter_booking_system.dto;

import jakarta.validation.constraints.NotEmpty;

public class BookingExtensionRequestDTO {

    @NotEmpty(message = "New duration label cannot be empty")
    private String newDurationLabel;

    // Getter and Setter
    public String getNewDurationLabel() {
        return newDurationLabel;
    }

    public void setNewDurationLabel(String newDurationLabel) {
        this.newDurationLabel = newDurationLabel;
    }
}