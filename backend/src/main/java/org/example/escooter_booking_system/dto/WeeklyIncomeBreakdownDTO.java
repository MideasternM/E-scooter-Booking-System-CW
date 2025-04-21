package org.example.escooter_booking_system.dto;

import java.math.BigDecimal;

// Using BigDecimal for currency precision
public class WeeklyIncomeBreakdownDTO {

    private BigDecimal income1Hour = BigDecimal.ZERO;
    private BigDecimal income4Hours = BigDecimal.ZERO;
    private BigDecimal income1Day = BigDecimal.ZERO;
    private BigDecimal income1Week = BigDecimal.ZERO;
    private BigDecimal incomeOther = BigDecimal.ZERO;

    // Getters and Setters
    public BigDecimal getIncome1Hour() {
        return income1Hour;
    }

    public void setIncome1Hour(BigDecimal income1Hour) {
        this.income1Hour = income1Hour;
    }

    public BigDecimal getIncome4Hours() {
        return income4Hours;
    }

    public void setIncome4Hours(BigDecimal income4Hours) {
        this.income4Hours = income4Hours;
    }

    public BigDecimal getIncome1Day() {
        return income1Day;
    }

    public void setIncome1Day(BigDecimal income1Day) {
        this.income1Day = income1Day;
    }

    public BigDecimal getIncome1Week() {
        return income1Week;
    }

    public void setIncome1Week(BigDecimal income1Week) {
        this.income1Week = income1Week;
    }

    public BigDecimal getIncomeOther() {
        return incomeOther;
    }

    public void setIncomeOther(BigDecimal incomeOther) {
        this.incomeOther = incomeOther;
    }

    // Convenience method to add income to the correct category
    public void addIncome(String category, BigDecimal amount) {
        if (amount == null)
            return;
        switch (category) {
            case "1 Hour":
                this.income1Hour = this.income1Hour.add(amount);
                break;
            case "4 Hours":
                this.income4Hours = this.income4Hours.add(amount);
                break;
            case "1 Day":
                this.income1Day = this.income1Day.add(amount);
                break;
            case "1 Week":
                this.income1Week = this.income1Week.add(amount);
                break;
            default: // "Other"
                this.incomeOther = this.incomeOther.add(amount);
                break;
        }
    }
}