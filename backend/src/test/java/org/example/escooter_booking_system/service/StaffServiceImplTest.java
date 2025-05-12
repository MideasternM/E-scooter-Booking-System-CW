package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.Staff;
import org.example.escooter_booking_system.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID; // For verifying token structure in authenticateStaff
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceImplTest {

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private StaffServiceImpl staffService;

    private Staff staff;

    @BeforeEach
    void setUp() {
        staff = new Staff();
        staff.setId(1L);
        staff.setName("Test Staff");
        staff.setStaffNumber("S123");
        staff.setPassword("password123");
        staff.setPosition("Manager");
        staff.setArea("North");
        staff.setStatus("Active");
        staff.setEmail("staff@example.com");
        staff.setPhoneNumber("1234567890");
    }

    @Test
    void addStaff_shouldSetCreationDateAndSave() {
        // Arrange
        Staff newStaff = new Staff();
        newStaff.setName("New Staff");
        // No createdAt set initially

        when(staffRepository.save(any(Staff.class))).thenReturn(newStaff);

        // Act
        Staff savedStaff = staffService.addStaff(newStaff);

        // Assert
        assertNotNull(savedStaff.getCreatedAt(), "Creation date should be set.");
        // Verify createdAt is recent (e.g., within the last 5 seconds)
        long timeDiff = new Date().getTime() - savedStaff.getCreatedAt().getTime();
        assertTrue(timeDiff < 5000, "CreatedAt should be a recent timestamp.");

        ArgumentCaptor<Staff> staffCaptor = ArgumentCaptor.forClass(Staff.class);
        verify(staffRepository, times(1)).save(staffCaptor.capture());
        assertEquals("New Staff", staffCaptor.getValue().getName());
        assertNotNull(staffCaptor.getValue().getCreatedAt(), "Captured staff should have creation date.");
    }

    @Test
    void getStaffById_whenStaffExists_shouldReturnStaff() {
        // Arrange
        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));

        // Act
        Staff foundStaff = staffService.getStaffById(1L);

        // Assert
        assertNotNull(foundStaff);
        assertEquals(staff.getId(), foundStaff.getId());
        assertEquals(staff.getName(), foundStaff.getName());
        verify(staffRepository, times(1)).findById(1L);
    }

    @Test
    void getStaffById_whenStaffDoesNotExist_shouldReturnNull() {
        // Arrange
        when(staffRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Staff foundStaff = staffService.getStaffById(2L);

        // Assert
        assertNull(foundStaff);
        verify(staffRepository, times(1)).findById(2L);
    }

    @Test
    void getAllStaff_whenStaffExist_shouldReturnStaffList() {
        // Arrange
        Staff staff2 = new Staff();
        staff2.setId(2L);
        staff2.setName("Another Staff");
        List<Staff> staffList = new ArrayList<>(Arrays.asList(staff, staff2));
        when(staffRepository.findAll()).thenReturn(staffList);

        // Act
        List<Staff> foundList = staffService.getAllStaff();

        // Assert
        assertNotNull(foundList);
        assertEquals(2, foundList.size());
        assertEquals(staff.getName(), foundList.get(0).getName());
        assertEquals(staff2.getName(), foundList.get(1).getName());
        verify(staffRepository, times(1)).findAll();
    }

    @Test
    void getAllStaff_whenNoStaffExist_shouldReturnEmptyList() {
        // Arrange
        when(staffRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Staff> foundList = staffService.getAllStaff();

        // Assert
        assertNotNull(foundList);
        assertTrue(foundList.isEmpty());
        verify(staffRepository, times(1)).findAll();
    }

    @Test
    void getStaffByNumber_whenStaffExists_shouldReturnStaff() {
        // Arrange
        String staffNumber = "S123";
        staff.setStaffNumber(staffNumber); // Ensure staff object in setUp has this number
        when(staffRepository.findByStaffNumber(staffNumber)).thenReturn(staff); // Assuming findByStaffNumber returns Staff or null

        // Act
        Staff foundStaff = staffService.getStaffByNumber(staffNumber);

        // Assert
        assertNotNull(foundStaff);
        assertEquals(staffNumber, foundStaff.getStaffNumber());
        assertEquals(staff.getName(), foundStaff.getName());
        verify(staffRepository, times(1)).findByStaffNumber(staffNumber);
    }

    @Test
    void getStaffByNumber_whenStaffDoesNotExist_shouldReturnNull() {
        // Arrange
        String staffNumber = "S999";
        when(staffRepository.findByStaffNumber(staffNumber)).thenReturn(null); // Assuming findByStaffNumber returns Staff or null

        // Act
        Staff foundStaff = staffService.getStaffByNumber(staffNumber);

        // Assert
        assertNull(foundStaff);
        verify(staffRepository, times(1)).findByStaffNumber(staffNumber);
    }

    @Test
    void updateStaff_whenStaffExists_shouldUpdateAndReturnStaff() {
        // Arrange
        Long staffId = staff.getId();
        Staff detailsToUpdate = new Staff();
        detailsToUpdate.setName("Updated Name");
        detailsToUpdate.setPosition("Senior Manager");
        detailsToUpdate.setPhoneNumber("0987654321");
        detailsToUpdate.setEmail("updated.staff@example.com");
        detailsToUpdate.setArea("South");
        // Note: staffNumber, password, status, createdAt are not updated by this method in StaffServiceImpl

        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff)); // Return the existing staff member from setUp
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved entity

        // Act
        Staff updatedStaff = staffService.updateStaff(staffId, detailsToUpdate);

        // Assert
        assertNotNull(updatedStaff);
        assertEquals(staffId, updatedStaff.getId());
        assertEquals("Updated Name", updatedStaff.getName());
        assertEquals("Senior Manager", updatedStaff.getPosition());
        assertEquals("0987654321", updatedStaff.getPhoneNumber());
        assertEquals("updated.staff@example.com", updatedStaff.getEmail());
        assertEquals("South", updatedStaff.getArea());

        // Verify that original non-updated fields remain unchanged
        assertEquals(staff.getStaffNumber(), updatedStaff.getStaffNumber()); // Should not change
        assertEquals(staff.getPassword(), updatedStaff.getPassword());     // Should not change
        assertEquals(staff.getStatus(), updatedStaff.getStatus());         // Should not change

        ArgumentCaptor<Staff> staffCaptor = ArgumentCaptor.forClass(Staff.class);
        verify(staffRepository, times(1)).save(staffCaptor.capture());
        Staff savedByRepo = staffCaptor.getValue();

        assertEquals("Updated Name", savedByRepo.getName());
        assertEquals(staff.getStaffNumber(), savedByRepo.getStaffNumber()); // Ensure original staff number is preserved
    }

    @Test
    void updateStaff_whenStaffDoesNotExist_shouldReturnNull() {
        // Arrange
        Long nonExistentId = 99L;
        Staff detailsToUpdate = new Staff();
        detailsToUpdate.setName("Updated Name");

        when(staffRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Staff updatedStaff = staffService.updateStaff(nonExistentId, detailsToUpdate);

        // Assert
        assertNull(updatedStaff);
        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    void deleteStaff_whenStaffExists_shouldCallDeleteById() {
        // Arrange
        Long staffId = staff.getId();
        // No need to mock findById for deleteById if the service doesn't check existence before deleting
        // StaffServiceImpl directly calls deleteById
        doNothing().when(staffRepository).deleteById(staffId);

        // Act
        staffService.deleteStaff(staffId);

        // Assert
        verify(staffRepository, times(1)).deleteById(staffId);
    }

    // Optional: Test deleteStaff when ID does not exist. 
    // Spring Data JPA's deleteById usually doesn't throw an error if ID not found.
    // So, the behavior is often just that deleteById is called.
    @Test
    void deleteStaff_whenStaffDoesNotExist_shouldStillCallDeleteById() {
        // Arrange
        Long nonExistentId = 99L;
        doNothing().when(staffRepository).deleteById(nonExistentId);

        // Act
        staffService.deleteStaff(nonExistentId);

        // Assert
        verify(staffRepository, times(1)).deleteById(nonExistentId);
    }

    @Test
    void getStaffByStatus_shouldReturnMatchingStaffList() {
        // Arrange
        String status = "Active";
        List<Staff> activeStaffList = new ArrayList<>(Arrays.asList(staff)); // staff from setUp is Active
        when(staffRepository.findByStatus(status)).thenReturn(activeStaffList);

        // Act
        List<Staff> foundList = staffService.getStaffByStatus(status);

        // Assert
        assertNotNull(foundList);
        assertEquals(1, foundList.size());
        assertEquals(status, foundList.get(0).getStatus());
        verify(staffRepository, times(1)).findByStatus(status);
    }

    @Test
    void getStaffByArea_shouldReturnMatchingStaffList() {
        // Arrange
        String area = "North";
        List<Staff> northAreaStaffList = new ArrayList<>(Arrays.asList(staff)); // staff from setUp is in North area
        when(staffRepository.findByArea(area)).thenReturn(northAreaStaffList);

        // Act
        List<Staff> foundList = staffService.getStaffByArea(area);

        // Assert
        assertNotNull(foundList);
        assertEquals(1, foundList.size());
        assertEquals(area, foundList.get(0).getArea());
        verify(staffRepository, times(1)).findByArea(area);
    }

    @Test
    void getStaffByPosition_shouldReturnMatchingStaffList() {
        // Arrange
        String position = "Manager";
        List<Staff> managerStaffList = new ArrayList<>(Arrays.asList(staff)); // staff from setUp is Manager
        when(staffRepository.findByPosition(position)).thenReturn(managerStaffList);

        // Act
        List<Staff> foundList = staffService.getStaffByPosition(position);

        // Assert
        assertNotNull(foundList);
        assertEquals(1, foundList.size());
        assertEquals(position, foundList.get(0).getPosition());
        verify(staffRepository, times(1)).findByPosition(position);
    }

    @Test
    void updateStaffStatus_whenStaffExists_shouldUpdateAndReturnStaff() {
        // Arrange
        Long staffId = staff.getId();
        String newStatus = "Inactive";
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Staff updatedStaff = staffService.updateStaffStatus(staffId, newStatus);

        // Assert
        assertNotNull(updatedStaff);
        assertEquals(newStatus, updatedStaff.getStatus());
        verify(staffRepository, times(1)).save(staff);
    }

    @Test
    void updateStaffStatus_whenStaffDoesNotExist_shouldReturnNull() {
        // Arrange
        Long nonExistentId = 99L;
        String newStatus = "Inactive";
        when(staffRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Staff updatedStaff = staffService.updateStaffStatus(nonExistentId, newStatus);

        // Assert
        assertNull(updatedStaff);
        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    void updateStaffArea_whenStaffExists_shouldUpdateAndReturnStaff() {
        // Arrange
        Long staffId = staff.getId();
        String newArea = "West";
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Staff updatedStaff = staffService.updateStaffArea(staffId, newArea);

        // Assert
        assertNotNull(updatedStaff);
        assertEquals(newArea, updatedStaff.getArea());
        verify(staffRepository, times(1)).save(staff);
    }

    @Test
    void updateStaffArea_whenStaffDoesNotExist_shouldReturnNull() {
        // Arrange
        Long nonExistentId = 99L;
        String newArea = "West";
        when(staffRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Staff updatedStaff = staffService.updateStaffArea(nonExistentId, newArea);

        // Assert
        assertNull(updatedStaff);
        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    void authenticateStaff_whenCredentialsAreValid_shouldReturnTokenAndStaffInfo() {
        // Arrange
        String staffNumber = staff.getStaffNumber();
        String password = staff.getPassword();
        when(staffRepository.findByStaffNumber(staffNumber)).thenReturn(staff);

        // Act
        Map<String, Object> result = staffService.authenticateStaff(staffNumber, password);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("token"));
        assertTrue(result.containsKey("staff"));
        assertNotNull(result.get("token"));
        assertEquals(staff, result.get("staff"));
        // Optionally, check token format if it's predictable (e.g., UUID)
        assertDoesNotThrow(() -> UUID.fromString(result.get("token").toString()));
        verify(staffRepository, times(1)).findByStaffNumber(staffNumber);
    }

    @Test
    void authenticateStaff_whenStaffNumberDoesNotExist_shouldReturnNull() {
        // Arrange
        String staffNumber = "SNonExistent";
        String password = "password123";
        when(staffRepository.findByStaffNumber(staffNumber)).thenReturn(null);

        // Act
        Map<String, Object> result = staffService.authenticateStaff(staffNumber, password);

        // Assert
        assertNull(result);
        verify(staffRepository, times(1)).findByStaffNumber(staffNumber);
    }

    @Test
    void authenticateStaff_whenPasswordIsIncorrect_shouldReturnNull() {
        // Arrange
        String staffNumber = staff.getStaffNumber();
        String incorrectPassword = "wrongPassword";
        when(staffRepository.findByStaffNumber(staffNumber)).thenReturn(staff);

        // Act
        Map<String, Object> result = staffService.authenticateStaff(staffNumber, incorrectPassword);

        // Assert
        assertNull(result);
        verify(staffRepository, times(1)).findByStaffNumber(staffNumber);
    }

    @Test
    void authenticateStaff_whenStaffPasswordIsNullInDB_shouldReturnNull() {
        // Arrange
        String staffNumber = staff.getStaffNumber();
        String passwordAttempt = "password123";
        Staff staffWithNullPassword = new Staff();
        staffWithNullPassword.setStaffNumber(staffNumber);
        staffWithNullPassword.setPassword(null); // Staff in DB has null password

        when(staffRepository.findByStaffNumber(staffNumber)).thenReturn(staffWithNullPassword);

        // Act
        Map<String, Object> result = staffService.authenticateStaff(staffNumber, passwordAttempt);

        // Assert
        assertNull(result);
        verify(staffRepository, times(1)).findByStaffNumber(staffNumber);
    }
} 