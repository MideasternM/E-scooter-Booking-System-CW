package org.example.escooter_booking_system.controller;

import org.example.escooter_booking_system.dto.WeeklyIncomeBreakdownDTO;
import org.example.escooter_booking_system.dto.DailyIncomeDTO;
import org.example.escooter_booking_system.dto.BookingDurationPopularityDTO;
import org.example.escooter_booking_system.service.PaymentService;
import org.example.escooter_booking_system.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics") // Base path for admin statistics
@CrossOrigin(origins = "http://localhost:8080") // Adjust if your frontend runs elsewhere
public class StatisticsController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/weekly-income-breakdown")
    public ResponseEntity<WeeklyIncomeBreakdownDTO> getWeeklyIncomeBreakdown() {
        WeeklyIncomeBreakdownDTO breakdown = paymentService.getWeeklyIncomeBreakdown();
        return ResponseEntity.ok(breakdown);
    }

    // Add endpoint for daily income for the current week
    @GetMapping("/daily-income-current-week")
    public ResponseEntity<DailyIncomeDTO> getDailyIncomeForCurrentWeek() {
        DailyIncomeDTO dailyIncome = paymentService.getDailyIncomeForCurrentWeek();
        return ResponseEntity.ok(dailyIncome);
    }

    // Add endpoint for booking duration popularity
    @GetMapping("/booking-duration-popularity")
    public ResponseEntity<BookingDurationPopularityDTO> getBookingDurationPopularity() {
        BookingDurationPopularityDTO popularity = bookingService.getBookingDurationPopularity();
        return ResponseEntity.ok(popularity);
    }

    // Add other statistics endpoints here in the future if needed
}