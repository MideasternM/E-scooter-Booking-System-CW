package org.example.escooter_booking_system.controller;

import org.example.escooter_booking_system.model.FaultReport;
import org.example.escooter_booking_system.service.FaultReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fault-reports")
public class FaultReportController {

    @Autowired
    private FaultReportService faultReportService;

    @PostMapping
    public ResponseEntity<FaultReport> createFaultReport(@RequestBody FaultReport faultReport) {
        FaultReport createdReport = faultReportService.createFaultReport(faultReport);
        return ResponseEntity.ok(createdReport);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FaultReport> getFaultReportById(@PathVariable Long id) {
        FaultReport faultReport = faultReportService.getFaultReportById(id);
        if (faultReport != null) {
            return ResponseEntity.ok(faultReport);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<FaultReport>> getFaultReportsByBooking(@PathVariable Long bookingId) {
        List<FaultReport> reports = faultReportService.getFaultReportsByBooking(bookingId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FaultReport>> getFaultReportsByUser(@PathVariable Long userId) {
        List<FaultReport> reports = faultReportService.getFaultReportsByUser(userId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/scooter/{scooterId}")
    public ResponseEntity<List<FaultReport>> getFaultReportsByScooter(@PathVariable Long scooterId) {
        List<FaultReport> reports = faultReportService.getFaultReportsByScooter(scooterId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<FaultReport>> getFaultReportsByStaff(@PathVariable Long staffId) {
        List<FaultReport> reports = faultReportService.getFaultReportsByStaff(staffId);
        return ResponseEntity.ok(reports);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FaultReport> updateFaultReport(
            @PathVariable Long id,
            @RequestBody FaultReport faultReportDetails) {
        FaultReport updatedReport = faultReportService.updateFaultReport(id, faultReportDetails);
        if (updatedReport != null) {
            return ResponseEntity.ok(updatedReport);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<FaultReport> assignToStaff(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        FaultReport assignedReport = faultReportService.assignToStaff(id, staffId);
        if (assignedReport != null) {
            return ResponseEntity.ok(assignedReport);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<FaultReport> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        FaultReport updatedReport = faultReportService.updateStatus(id, status);
        if (updatedReport != null) {
            return ResponseEntity.ok(updatedReport);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/resolution")
    public ResponseEntity<FaultReport> addResolution(
            @PathVariable Long id,
            @RequestParam String resolution) {
        FaultReport resolvedReport = faultReportService.addResolution(id, resolution);
        if (resolvedReport != null) {
            return ResponseEntity.ok(resolvedReport);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FaultReport>> getFaultReportsByStatus(@PathVariable String status) {
        List<FaultReport> reports = faultReportService.getFaultReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/type/{faultType}")
    public ResponseEntity<List<FaultReport>> getFaultReportsByType(@PathVariable String faultType) {
        List<FaultReport> reports = faultReportService.getFaultReportsByType(faultType);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<FaultReport>> getFaultReportsBySeverity(@PathVariable String severity) {
        List<FaultReport> reports = faultReportService.getFaultReportsBySeverity(severity);
        return ResponseEntity.ok(reports);
    }
} 