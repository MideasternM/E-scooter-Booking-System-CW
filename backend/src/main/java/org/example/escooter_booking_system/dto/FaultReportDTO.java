package org.example.escooter_booking_system.dto;

// No Lombok imports needed

public class FaultReportDTO {
    private Long bookingId;
    private String faultType;
    private String description;
    private String severity;
    private String location;
    // 可以根据需要添加其他字段，例如 userId 用于校验
    // private Long userId;

    // Getters
    public Long getBookingId() {
        return bookingId;
    }

    public String getFaultType() {
        return faultType;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }

    public String getLocation() {
        return location;
    }

    // Setters
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public void setFaultType(String faultType) {
        this.faultType = faultType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}