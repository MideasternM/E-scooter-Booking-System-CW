package org.example.escooter_booking_system.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.escooter_booking_system.dto.FaultReportDTO;
import org.example.escooter_booking_system.model.Booking;
import org.example.escooter_booking_system.model.FaultReport;
import org.example.escooter_booking_system.model.Scooter;
import org.example.escooter_booking_system.model.Staff;
import org.example.escooter_booking_system.model.User;
import org.example.escooter_booking_system.repository.BookingRepository;
import org.example.escooter_booking_system.repository.FaultReportRepository;
import org.example.escooter_booking_system.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaultReportServiceImplTest {

    @Mock
    private FaultReportRepository faultReportRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private FaultReportServiceImpl faultReportService;

    private FaultReportDTO faultReportDTO;
    private Booking mockBooking;
    private User mockUser;
    private Scooter mockScooter;
    private FaultReport report1;
    private FaultReport report2;
    private Staff testStaff;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");

        mockScooter = new Scooter();
        mockScooter.setId(10L);
        mockScooter.setScooterCode("S100");

        mockBooking = new Booking();
        mockBooking.setId(100L);
        mockBooking.setUser(mockUser);
        mockBooking.setScooter(mockScooter);

        testStaff = new Staff();
        testStaff.setId(5L);
        testStaff.setStaffNumber("EMP005");

        report1 = new FaultReport();
        report1.setId(1L);
        report1.setBooking(mockBooking);
        report1.setReportedBy(mockUser);
        report1.setScooter(mockScooter);
        report1.setFaultType("MECHANICAL");
        report1.setDescription("Brake not working");
        report1.setSeverity("HIGH");
        report1.setStatus("NEW");
        report1.setLocation("Central Park");
        report1.setReportedAt(new Date());

        report2 = new FaultReport();
        report2.setId(2L);
        report2.setBooking(mockBooking);
        report2.setReportedBy(mockUser);
        report2.setScooter(mockScooter);
        report2.setFaultType("ELECTRICAL");
        report2.setDescription("Light broken");
        report2.setSeverity("MEDIUM");
        report2.setStatus("ASSIGNED");
        report2.setAssignedStaff(testStaff);
        report2.setLocation("Downtown");
        report2.setReportedAt(new Date(System.currentTimeMillis() - 100000));
        report2.setAssignedAt(new Date());

        faultReportDTO = new FaultReportDTO();
        faultReportDTO.setBookingId(mockBooking.getId());
        faultReportDTO.setFaultType("Flat Tire");
        faultReportDTO.setDescription("The front tire is flat.");
        faultReportDTO.setSeverity("HIGH");
        faultReportDTO.setLocation("Near Central Park");
    }

    @Test
    void createFaultReport_whenBookingExists_shouldCreateAndSaveReport() {
        // Arrange
        when(bookingRepository.findById(faultReportDTO.getBookingId())).thenReturn(Optional.of(mockBooking));
        when(faultReportRepository.save(any(FaultReport.class))).thenAnswer(invocation -> {
            FaultReport savedReport = invocation.getArgument(0);
            savedReport.setId(1L); // Simulate saving and getting an ID
            return savedReport;
        });

        // Act
        FaultReport createdReport = faultReportService.createFaultReport(faultReportDTO);

        // Assert
        assertNotNull(createdReport);
        assertNotNull(createdReport.getId());
        assertEquals(mockBooking, createdReport.getBooking());
        assertEquals(mockUser, createdReport.getReportedBy());
        assertEquals(mockScooter, createdReport.getScooter());
        assertEquals(faultReportDTO.getFaultType(), createdReport.getFaultType());
        assertEquals(faultReportDTO.getDescription(), createdReport.getDescription());
        assertEquals(faultReportDTO.getSeverity(), createdReport.getSeverity());
        assertEquals(faultReportDTO.getLocation(), createdReport.getLocation());
        assertEquals("NEW", createdReport.getStatus());
        assertNotNull(createdReport.getReportedAt());

        // Verify reportedAt is recent (e.g., within the last 5 seconds)
        long timeDiff = new Date().getTime() - createdReport.getReportedAt().getTime();
        assertTrue(timeDiff < 5000, "ReportedAt should be a recent timestamp.");

        ArgumentCaptor<FaultReport> faultReportCaptor = ArgumentCaptor.forClass(FaultReport.class);
        verify(faultReportRepository, times(1)).save(faultReportCaptor.capture());
        FaultReport capturedReport = faultReportCaptor.getValue();

        assertEquals(mockBooking.getId(), capturedReport.getBooking().getId());
        assertEquals(mockUser.getId(), capturedReport.getReportedBy().getId());
        // scooter can be null if booking.getScooter() is null, but in our setup it's not.
        if (mockScooter != null) {
             assertEquals(mockScooter.getId(), capturedReport.getScooter().getId());
        }
        assertEquals(faultReportDTO.getFaultType(), capturedReport.getFaultType());
        assertEquals("NEW", capturedReport.getStatus());
    }

    @Test
    void createFaultReport_whenBookingNotFound_shouldThrowEntityNotFoundException() {
        // Arrange
        Long nonExistentBookingId = 999L;
        faultReportDTO.setBookingId(nonExistentBookingId);
        when(bookingRepository.findById(nonExistentBookingId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            faultReportService.createFaultReport(faultReportDTO);
        });

        assertTrue(exception.getMessage().contains("Booking not found with id: " + nonExistentBookingId));
        verify(faultReportRepository, never()).save(any(FaultReport.class));
    }

    // --- Tests for getFaultReportsByUser ---
    @Test
    void getFaultReportsByUser_whenUserHasReports_shouldReturnReportList() {
        // Arrange
        Long userId = mockUser.getId();
        List<FaultReport> userReports = Arrays.asList(report1, report2);
        when(faultReportRepository.findByReportedById(userId)).thenReturn(userReports);

        // Act
        List<FaultReport> result = faultReportService.getFaultReportsByUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(report1.getId(), result.get(0).getId());
        assertEquals(report2.getId(), result.get(1).getId());
        verify(faultReportRepository, times(1)).findByReportedById(userId);
    }

    @Test
    void getFaultReportsByUser_whenUserHasNoReports_shouldReturnEmptyList() {
        // Arrange
        Long userId = mockUser.getId();
        when(faultReportRepository.findByReportedById(userId)).thenReturn(Collections.emptyList());

        // Act
        List<FaultReport> result = faultReportService.getFaultReportsByUser(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(faultReportRepository, times(1)).findByReportedById(userId);
    }

    // --- Test getAllFaultReports ---
    @Test
    void getAllFaultReports_whenReportsExist_shouldReturnAllReports() {
        // Arrange
        List<FaultReport> allReports = Arrays.asList(report1, report2);
        when(faultReportRepository.findAll()).thenReturn(allReports);

        // Act
        List<FaultReport> result = faultReportService.getAllFaultReports();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(report1));
        assertTrue(result.contains(report2));
        verify(faultReportRepository, times(1)).findAll();
    }

    @Test
    void getAllFaultReports_whenNoReportsExist_shouldReturnEmptyList() {
        // Arrange
        when(faultReportRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<FaultReport> result = faultReportService.getAllFaultReports();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(faultReportRepository, times(1)).findAll();
    }

    // --- Test getFaultReportById ---
    @Test
    void getFaultReportById_whenReportExists_shouldReturnReport() {
        // Arrange
        Long reportId = report1.getId();
        when(faultReportRepository.findById(reportId)).thenReturn(Optional.of(report1));

        // Act
        FaultReport result = faultReportService.getFaultReportById(reportId);

        // Assert
        assertNotNull(result);
        assertEquals(reportId, result.getId());
        assertEquals(report1.getDescription(), result.getDescription());
        verify(faultReportRepository, times(1)).findById(reportId);
    }

    @Test
    void getFaultReportById_whenReportDoesNotExist_shouldReturnNull() {
        // Arrange
        Long nonExistentId = 999L;
        when(faultReportRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        FaultReport result = faultReportService.getFaultReportById(nonExistentId);

        // Assert
        assertNull(result);
        verify(faultReportRepository, times(1)).findById(nonExistentId);
    }

    // --- Test assignToStaff ---
    @Test
    void assignToStaff_whenReportAndStaffExist_shouldAssignAndReturnUpdatedReport() {
        // Arrange
        Long reportId = report1.getId(); // report1 is NEW and unassigned
        Long staffId = testStaff.getId();

        when(faultReportRepository.findById(reportId)).thenReturn(Optional.of(report1));
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(testStaff));
        
        ArgumentCaptor<FaultReport> reportCaptor = ArgumentCaptor.forClass(FaultReport.class);
        when(faultReportRepository.save(reportCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FaultReport result = faultReportService.assignToStaff(reportId, staffId);

        // Assert
        assertNotNull(result);
        assertEquals(testStaff, result.getAssignedStaff());
        assertEquals("ASSIGNED", result.getStatus());
        assertNotNull(result.getAssignedAt());
        // Verify captured argument for save
        FaultReport savedReport = reportCaptor.getValue();
        assertEquals(testStaff, savedReport.getAssignedStaff());
        assertEquals("ASSIGNED", savedReport.getStatus());
        assertNotNull(savedReport.getAssignedAt());

        verify(faultReportRepository, times(1)).findById(reportId);
        verify(staffRepository, times(1)).findById(staffId);
        verify(faultReportRepository, times(1)).save(any(FaultReport.class));
    }

    @Test
    void assignToStaff_whenReportDoesNotExist_shouldReturnNull() {
        // Arrange
        Long nonExistentReportId = 999L;
        Long staffId = testStaff.getId();

        when(faultReportRepository.findById(nonExistentReportId)).thenReturn(Optional.empty());
        // staffRepository.findById might not be called if report is not found first, good to verify

        // Act
        FaultReport result = faultReportService.assignToStaff(nonExistentReportId, staffId);

        // Assert
        assertNull(result);
        verify(faultReportRepository, times(1)).findById(nonExistentReportId);
        verify(staffRepository, never()).findById(staffId); // Should not attempt to find staff if report not found
        verify(faultReportRepository, never()).save(any(FaultReport.class));
    }

    @Test
    void assignToStaff_whenStaffDoesNotExist_shouldReturnNull() {
        // Arrange
        Long reportId = report1.getId();
        Long nonExistentStaffId = 998L;

        when(faultReportRepository.findById(reportId)).thenReturn(Optional.of(report1));
        when(staffRepository.findById(nonExistentStaffId)).thenReturn(Optional.empty());

        // Act
        FaultReport result = faultReportService.assignToStaff(reportId, nonExistentStaffId);

        // Assert
        assertNull(result);
        verify(faultReportRepository, times(1)).findById(reportId);
        verify(staffRepository, times(1)).findById(nonExistentStaffId);
        verify(faultReportRepository, never()).save(any(FaultReport.class));
    }

    // --- Test updateStatus ---
    @Test
    void updateStatus_toInProgress_shouldSetStatusAndStartedAt() {
        // Arrange
        Long reportId = report2.getId(); // report2 is ASSIGNED
        report2.setStartedAt(null); // Ensure startedAt is null initially for this test
        String newStatus = "IN_PROGRESS";
        String notes = "Investigation started.";

        when(faultReportRepository.findById(reportId)).thenReturn(Optional.of(report2));
        ArgumentCaptor<FaultReport> reportCaptor = ArgumentCaptor.forClass(FaultReport.class);
        when(faultReportRepository.save(reportCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FaultReport result = faultReportService.updateStatus(reportId, newStatus, notes);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertEquals(notes, result.getStaffNotes());
        assertNotNull(result.getStartedAt());
        // Verify captured argument
        FaultReport savedReport = reportCaptor.getValue();
        assertEquals(newStatus, savedReport.getStatus());
        assertNotNull(savedReport.getStartedAt());
        verify(faultReportRepository, times(1)).save(report2);
    }

    @Test
    void updateStatus_toResolved_shouldSetStatusAndResolvedAt() {
        // Arrange
        Long reportId = report2.getId(); 
        report2.setStatus("IN_PROGRESS"); // Assume it was in progress
        report2.setResolvedAt(null);
        String newStatus = "RESOLVED";
        String notes = "Issue fully resolved.";

        when(faultReportRepository.findById(reportId)).thenReturn(Optional.of(report2));
        when(faultReportRepository.save(any(FaultReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FaultReport result = faultReportService.updateStatus(reportId, newStatus, notes);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertEquals(notes, result.getStaffNotes());
        assertNotNull(result.getResolvedAt());
        verify(faultReportRepository, times(1)).save(report2);
    }

    @Test
    void updateStatus_toOtherStatusWithNotes_shouldUpdateStatusAndNotes() {
        // Arrange
        Long reportId = report1.getId(); // report1 is NEW
        String newStatus = "WAITING_FOR_PARTS";
        String notes = "Part XYZ ordered.";
        Date originalStartedAt = report1.getStartedAt(); // Should be null if not started
        Date originalResolvedAt = report1.getResolvedAt(); // Should be null if not resolved

        when(faultReportRepository.findById(reportId)).thenReturn(Optional.of(report1));
        when(faultReportRepository.save(any(FaultReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FaultReport result = faultReportService.updateStatus(reportId, newStatus, notes);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertEquals(notes, result.getStaffNotes());
        assertEquals(originalStartedAt, result.getStartedAt(), "StartedAt should not change for this status update.");
        assertEquals(originalResolvedAt, result.getResolvedAt(), "ResolvedAt should not change for this status update.");
        verify(faultReportRepository, times(1)).save(report1);
    }
    
    @Test
    void updateStatus_whenNotesAreNullOrEmpty_shouldStillUpdateStatus() {
        // Arrange
        Long reportId = report1.getId(); // report1 is NEW
        String newStatus = "PENDING_REVIEW";
        report1.setStaffNotes("Initial note"); // Pre-set a note

        when(faultReportRepository.findById(reportId)).thenReturn(Optional.of(report1));
        when(faultReportRepository.save(any(FaultReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act with null notes
        FaultReport resultNullNotes = faultReportService.updateStatus(reportId, newStatus, null);
        // Assert
        assertNotNull(resultNullNotes);
        assertEquals(newStatus, resultNullNotes.getStatus());
        assertEquals("Initial note", resultNullNotes.getStaffNotes(), "Staff notes should not change if new notes are null.");

        // Act with empty notes
        report1.setStatus("NEW"); // Reset status for next part of test
        FaultReport resultEmptyNotes = faultReportService.updateStatus(reportId, newStatus, "");
        // Assert
        assertNotNull(resultEmptyNotes);
        assertEquals(newStatus, resultEmptyNotes.getStatus());
        assertEquals("Initial note", resultEmptyNotes.getStaffNotes(), "Staff notes should not change if new notes are empty.");
        verify(faultReportRepository, times(2)).save(report1); // Called twice
    }

    @Test
    void updateStatus_whenReportDoesNotExist_shouldReturnNull() {
        // Arrange
        Long nonExistentReportId = 999L;
        String newStatus = "RESOLVED";
        String notes = "Attempt to resolve non-existent report.";
        when(faultReportRepository.findById(nonExistentReportId)).thenReturn(Optional.empty());

        // Act
        FaultReport result = faultReportService.updateStatus(nonExistentReportId, newStatus, notes);

        // Assert
        assertNull(result);
        verify(faultReportRepository, never()).save(any(FaultReport.class));
    }

    // --- Test addResolution ---
    @Test
    void addResolution_whenStatusIsInProgress_shouldSetResolutionAndStatusToResolvedAndResolvedAt() {
        // Arrange
        Long reportId = report2.getId();
        report2.setStatus("IN_PROGRESS");
        report2.setResolvedAt(null); // Ensure resolvedAt is null initially
        String resolutionText = "Replaced the flux capacitor.";

        when(faultReportRepository.findById(reportId)).thenReturn(Optional.of(report2));
        ArgumentCaptor<FaultReport> reportCaptor = ArgumentCaptor.forClass(FaultReport.class);
        when(faultReportRepository.save(reportCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FaultReport result = faultReportService.addResolution(reportId, resolutionText);

        // Assert
        assertNotNull(result);
        assertEquals(resolutionText, result.getResolution());
        assertEquals("RESOLVED", result.getStatus());
        assertNotNull(result.getResolvedAt());
        // Verify captured argument
        FaultReport savedReport = reportCaptor.getValue();
        assertEquals(resolutionText, savedReport.getResolution());
        assertEquals("RESOLVED", savedReport.getStatus());
        assertNotNull(savedReport.getResolvedAt());
        verify(faultReportRepository, times(1)).save(report2);
    }

    @Test
    void addResolution_whenStatusIsNotInProgress_shouldSetResolutionButNotChangeStatusOrResolvedAt() {
        // Arrange
        Long reportId = report1.getId(); // report1 status is NEW
        String originalStatus = report1.getStatus();
        Date originalResolvedAt = report1.getResolvedAt(); // Should be null
        String resolutionText = "Cleaned and checked.";

        when(faultReportRepository.findById(reportId)).thenReturn(Optional.of(report1));
        when(faultReportRepository.save(any(FaultReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FaultReport result = faultReportService.addResolution(reportId, resolutionText);

        // Assert
        assertNotNull(result);
        assertEquals(resolutionText, result.getResolution());
        assertEquals(originalStatus, result.getStatus(), "Status should not change if not IN_PROGRESS.");
        assertEquals(originalResolvedAt, result.getResolvedAt(), "ResolvedAt should not be set if status not changed to RESOLVED.");
        verify(faultReportRepository, times(1)).save(report1);
    }

    @Test
    void addResolution_whenReportDoesNotExist_shouldReturnNull() {
        // Arrange
        Long nonExistentReportId = 999L;
        String resolutionText = "Attempt to resolve non-existent report.";
        when(faultReportRepository.findById(nonExistentReportId)).thenReturn(Optional.empty());

        // Act
        FaultReport result = faultReportService.addResolution(nonExistentReportId, resolutionText);

        // Assert
        assertNull(result);
        verify(faultReportRepository, never()).save(any(FaultReport.class));
    }

    // --- Test getFaultReportsByStatus ---
    @Test
    void getFaultReportsByStatus_whenReportsExistForStatus_shouldReturnReportList() {
        // Arrange
        String status = "NEW";
        List<FaultReport> reportsWithStatus = Collections.singletonList(report1); // report1 is NEW
        when(faultReportRepository.findByStatus(status)).thenReturn(reportsWithStatus);

        // Act
        List<FaultReport> result = faultReportService.getFaultReportsByStatus(status);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(report1, result.get(0));
        verify(faultReportRepository, times(1)).findByStatus(status);
    }

    @Test
    void getFaultReportsByStatus_whenNoReportsExistForStatus_shouldReturnEmptyList() {
        // Arrange
        String status = "OBSOLETE";
        when(faultReportRepository.findByStatus(status)).thenReturn(Collections.emptyList());

        // Act
        List<FaultReport> result = faultReportService.getFaultReportsByStatus(status);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(faultReportRepository, times(1)).findByStatus(status);
    }

    // --- Test getFaultReportsByType ---
    @Test
    void getFaultReportsByType_whenReportsExistForType_shouldReturnReportList() {
        // Arrange
        String faultType = "MECHANICAL"; // report1 is MECHANICAL
        List<FaultReport> reportsWithType = Collections.singletonList(report1);
        when(faultReportRepository.findByFaultType(faultType)).thenReturn(reportsWithType);

        // Act
        List<FaultReport> result = faultReportService.getFaultReportsByType(faultType);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(report1, result.get(0));
        verify(faultReportRepository, times(1)).findByFaultType(faultType);
    }

    @Test
    void getFaultReportsByType_whenNoReportsExistForType_shouldReturnEmptyList() {
        // Arrange
        String faultType = "COSMETIC";
        when(faultReportRepository.findByFaultType(faultType)).thenReturn(Collections.emptyList());

        // Act
        List<FaultReport> result = faultReportService.getFaultReportsByType(faultType);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(faultReportRepository, times(1)).findByFaultType(faultType);
    }

    // --- Test getFaultReportsBySeverity ---
    @Test
    void getFaultReportsBySeverity_whenReportsExistForSeverity_shouldReturnReportList() {
        // Arrange
        String severity = "HIGH"; // report1 is HIGH
        List<FaultReport> reportsWithSeverity = Collections.singletonList(report1);
        when(faultReportRepository.findBySeverity(severity)).thenReturn(reportsWithSeverity);

        // Act
        List<FaultReport> result = faultReportService.getFaultReportsBySeverity(severity);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(report1, result.get(0));
        verify(faultReportRepository, times(1)).findBySeverity(severity);
    }

    @Test
    void getFaultReportsBySeverity_whenNoReportsExistForSeverity_shouldReturnEmptyList() {
        // Arrange
        String severity = "LOW";
        when(faultReportRepository.findBySeverity(severity)).thenReturn(Collections.emptyList());

        // Act
        List<FaultReport> result = faultReportService.getFaultReportsBySeverity(severity);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(faultReportRepository, times(1)).findBySeverity(severity);
    }

    // --- Test getFaultReportsByAssignedStaff ---
    @Test
    void getFaultReportsByAssignedStaff_whenReportsExistForStaff_shouldReturnReportList() {
        // Arrange
        Long staffId = testStaff.getId(); // report2 is assigned to testStaff
        List<FaultReport> reportsForStaff = Collections.singletonList(report2);
        when(faultReportRepository.findByAssignedStaffId(staffId)).thenReturn(reportsForStaff);

        // Act
        List<FaultReport> result = faultReportService.getFaultReportsByStaff(staffId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(report2, result.get(0));
        verify(faultReportRepository, times(1)).findByAssignedStaffId(staffId);
    }

    @Test
    void getFaultReportsByAssignedStaff_whenNoReportsExistForStaff_shouldReturnEmptyList() {
        // Arrange
        Long staffId = 999L; // Non-existent staff or staff with no reports
        when(faultReportRepository.findByAssignedStaffId(staffId)).thenReturn(Collections.emptyList());

        // Act
        List<FaultReport> result = faultReportService.getFaultReportsByStaff(staffId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(faultReportRepository, times(1)).findByAssignedStaffId(staffId);
    }
} 