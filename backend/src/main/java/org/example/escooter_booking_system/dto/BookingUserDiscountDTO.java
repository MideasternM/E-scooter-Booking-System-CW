package org.example.escooter_booking_system.dto;

/**
 * DTO for returning user discount information.
 */
public class BookingUserDiscountDTO {
    private boolean eligibleForDiscount;
    private double discountRate;
    private double weeklyUsageHours;
    private double requiredHoursForDiscount;

    // Default constructor
    public BookingUserDiscountDTO() {
    }

    // Constructor with all fields
    public BookingUserDiscountDTO(boolean eligibleForDiscount, double discountRate, double weeklyUsageHours,
            double requiredHoursForDiscount) {
        this.eligibleForDiscount = eligibleForDiscount;
        this.discountRate = discountRate;
        this.weeklyUsageHours = weeklyUsageHours;
        this.requiredHoursForDiscount = requiredHoursForDiscount;
    }

    // Getters and Setters
    public boolean isEligibleForDiscount() {
        return eligibleForDiscount;
    }

    public void setEligibleForDiscount(boolean eligibleForDiscount) {
        this.eligibleForDiscount = eligibleForDiscount;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getWeeklyUsageHours() {
        return weeklyUsageHours;
    }

    public void setWeeklyUsageHours(double weeklyUsageHours) {
        this.weeklyUsageHours = weeklyUsageHours;
    }

    public double getRequiredHoursForDiscount() {
        return requiredHoursForDiscount;
    }

    public void setRequiredHoursForDiscount(double requiredHoursForDiscount) {
        this.requiredHoursForDiscount = requiredHoursForDiscount;
    }
}