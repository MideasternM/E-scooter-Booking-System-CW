package org.example.escooter_booking_system.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.escooter_booking_system.dto.FaultReportDTO;
import org.example.escooter_booking_system.model.Booking;
import org.example.escooter_booking_system.model.FaultReport;
import org.example.escooter_booking_system.model.Staff;
import org.example.escooter_booking_system.repository.BookingRepository;
import org.example.escooter_booking_system.repository.FaultReportRepository;
import org.example.escooter_booking_system.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class FaultReportServiceImpl implements FaultReportService {

    @Autowired
    private FaultReportRepository faultReportRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public FaultReport createFaultReport(FaultReportDTO faultReportDTO) {
        Booking booking = bookingRepository.findById(faultReportDTO.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Booking not found with id: " + faultReportDTO.getBookingId()));

        FaultReport faultReport = new FaultReport();

        faultReport.setBooking(booking);
        faultReport.setReportedBy(booking.getUser());
        faultReport.setScooter(booking.getScooter());

        faultReport.setFaultType(faultReportDTO.getFaultType());
        faultReport.setDescription(faultReportDTO.getDescription());
        faultReport.setSeverity(faultReportDTO.getSeverity());
        faultReport.setLocation(faultReportDTO.getLocation());

        faultReport.setStatus("NEW");
        faultReport.setReportedAt(new Date());

        return faultReportRepository.save(faultReport);
    }

    @Override
    public FaultReport getFaultReportById(Long id) {
        return faultReportRepository.findById(id).orElse(null);
    }

    @Override
    public List<FaultReport> getFaultReportsByBooking(Long bookingId) {
        return faultReportRepository.findByBookingId(bookingId);
    }

    @Override
    public List<FaultReport> getFaultReportsByUser(Long userId) {
        return faultReportRepository.findByReportedById(userId);
    }

    @Override
    public List<FaultReport> getFaultReportsByScooter(Long scooterId) {
        return faultReportRepository.findByScooterId(scooterId);
    }

    @Override
    public List<FaultReport> getFaultReportsByStaff(Long staffId) {
        return faultReportRepository.findByAssignedStaffId(staffId);
    }

    @Override
    public FaultReport updateFaultReport(Long id, FaultReport faultReportDetails) {
        FaultReport existingReport = faultReportRepository.findById(id).orElse(null);
        if (existingReport != null) {
            existingReport.setFaultType(faultReportDetails.getFaultType());
            existingReport.setDescription(faultReportDetails.getDescription());
            existingReport.setSeverity(faultReportDetails.getSeverity());
            existingReport.setLocation(faultReportDetails.getLocation());
            existingReport.setRequiresPartReplacement(faultReportDetails.getRequiresPartReplacement());
            existingReport.setReplacementParts(faultReportDetails.getReplacementParts());
            return faultReportRepository.save(existingReport);
        }
        return null;
    }

    @Override
    public FaultReport assignToStaff(Long faultReportId, Long staffId) {
        FaultReport faultReport = faultReportRepository.findById(faultReportId).orElse(null);
        Staff staff = staffRepository.findById(staffId).orElse(null);

        if (faultReport != null && staff != null) {
            faultReport.setAssignedStaff(staff);
            faultReport.setStatus("ASSIGNED");
            faultReport.setAssignedAt(new Date());
            return faultReportRepository.save(faultReport);
        }
        return null;
    }

    @Override
    public FaultReport updateStatus(Long id, String status, String notes) {
        FaultReport faultReport = faultReportRepository.findById(id).orElse(null);
        if (faultReport != null) {
            faultReport.setStatus(status);
            if (notes != null && !notes.isEmpty()) {
                faultReport.setStaffNotes(notes);
            }
            if ("IN_PROGRESS".equalsIgnoreCase(status) && faultReport.getStartedAt() == null) {
                faultReport.setStartedAt(new Date());
            }
            if (status.equals("RESOLVED")) {
                faultReport.setResolvedAt(new Date());
            }
            return faultReportRepository.save(faultReport);
        }
        return null;
    }

    @Override
    public FaultReport addResolution(Long id, String resolution) {
        FaultReport faultReport = faultReportRepository.findById(id).orElse(null);
        if (faultReport != null) {
            faultReport.setResolution(resolution);
            if (faultReport.getStatus().equals("IN_PROGRESS")) {
                faultReport.setStatus("RESOLVED");
                faultReport.setResolvedAt(new Date());
            }
            return faultReportRepository.save(faultReport);
        }
        return null;
    }

    @Override
    public List<FaultReport> getFaultReportsByStatus(String status) {
        return faultReportRepository.findByStatus(status);
    }

    @Override
    public List<FaultReport> getFaultReportsByType(String faultType) {
        return faultReportRepository.findByFaultType(faultType);
    }

    @Override
    public List<FaultReport> getFaultReportsBySeverity(String severity) {
        return faultReportRepository.findBySeverity(severity);
    }

    @Override
    public List<FaultReport> getAllFaultReports() {
        return faultReportRepository.findAll();
    }
}