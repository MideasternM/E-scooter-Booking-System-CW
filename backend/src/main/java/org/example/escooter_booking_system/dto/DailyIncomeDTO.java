package org.example.escooter_booking_system.dto;

import java.math.BigDecimal;

public class DailyIncomeDTO {

    private BigDecimal mondayIncome = BigDecimal.ZERO;
    private BigDecimal tuesdayIncome = BigDecimal.ZERO;
    private BigDecimal wednesdayIncome = BigDecimal.ZERO;
    private BigDecimal thursdayIncome = BigDecimal.ZERO;
    private BigDecimal fridayIncome = BigDecimal.ZERO;
    private BigDecimal saturdayIncome = BigDecimal.ZERO;
    private BigDecimal sundayIncome = BigDecimal.ZERO;

    // Getters and Setters
    public BigDecimal getMondayIncome() {
        return mondayIncome;
    }

    public void setMondayIncome(BigDecimal mondayIncome) {
        this.mondayIncome = mondayIncome;
    }

    public BigDecimal getTuesdayIncome() {
        return tuesdayIncome;
    }

    public void setTuesdayIncome(BigDecimal tuesdayIncome) {
        this.tuesdayIncome = tuesdayIncome;
    }

    public BigDecimal getWednesdayIncome() {
        return wednesdayIncome;
    }

    public void setWednesdayIncome(BigDecimal wednesdayIncome) {
        this.wednesdayIncome = wednesdayIncome;
    }

    public BigDecimal getThursdayIncome() {
        return thursdayIncome;
    }

    public void setThursdayIncome(BigDecimal thursdayIncome) {
        this.thursdayIncome = thursdayIncome;
    }

    public BigDecimal getFridayIncome() {
        return fridayIncome;
    }

    public void setFridayIncome(BigDecimal fridayIncome) {
        this.fridayIncome = fridayIncome;
    }

    public BigDecimal getSaturdayIncome() {
        return saturdayIncome;
    }

    public void setSaturdayIncome(BigDecimal saturdayIncome) {
        this.saturdayIncome = saturdayIncome;
    }

    public BigDecimal getSundayIncome() {
        return sundayIncome;
    }

    public void setSundayIncome(BigDecimal sundayIncome) {
        this.sundayIncome = sundayIncome;
    }

    // Convenience method to add income based on Calendar day constants (e.g.,
    // Calendar.MONDAY)
    public void addIncome(int dayOfWeek, BigDecimal amount) {
        if (amount == null)
            return;
        switch (dayOfWeek) {
            case java.util.Calendar.MONDAY:
                this.mondayIncome = this.mondayIncome.add(amount);
                break;
            case java.util.Calendar.TUESDAY:
                this.tuesdayIncome = this.tuesdayIncome.add(amount);
                break;
            case java.util.Calendar.WEDNESDAY:
                this.wednesdayIncome = this.wednesdayIncome.add(amount);
                break;
            case java.util.Calendar.THURSDAY:
                this.thursdayIncome = this.thursdayIncome.add(amount);
                break;
            case java.util.Calendar.FRIDAY:
                this.fridayIncome = this.fridayIncome.add(amount);
                break;
            case java.util.Calendar.SATURDAY:
                this.saturdayIncome = this.saturdayIncome.add(amount);
                break;
            case java.util.Calendar.SUNDAY:
                this.sundayIncome = this.sundayIncome.add(amount);
                break;
            // Ignore other potential values
        }
    }
}