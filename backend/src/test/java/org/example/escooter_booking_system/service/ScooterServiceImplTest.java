package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.Scooter;
import org.example.escooter_booking_system.repository.ScooterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScooterServiceImplTest {

    @Mock
    private ScooterRepository scooterRepository;

    @InjectMocks
    private ScooterServiceImpl scooterService;

    private Scooter scooter1;
    private Scooter scooter2;

    @BeforeEach
    void setUp() {
        scooter1 = new Scooter();
        scooter1.setId(1L);
        scooter1.setScooterCode("S001");
        scooter1.setModel("Model X");
        scooter1.setBatteryLevel(new BigDecimal("85.0"));
        scooter1.setLocation("Central Park");
        scooter1.setAvailable(true);

        scooter2 = new Scooter();
        scooter2.setId(2L);
        scooter2.setScooterCode("S002");
        scooter2.setModel("Model Y");
        scooter2.setBatteryLevel(new BigDecimal("60.0"));
        scooter2.setLocation("Downtown");
        scooter2.setAvailable(false); // This one is not available
    }

    // --- 测试 getAvailableScooters ---
    @Test
    void getAvailableScooters_whenNoScootersAvailable_shouldReturnEmptyList() {
        // Arrange
        when(scooterRepository.findByIsAvailable(true)).thenReturn(Collections.emptyList());

        // Act
        List<Scooter> result = scooterService.getAvailableScooters();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(scooterRepository, times(1)).findByIsAvailable(true);
    }

    @Test
    void getAvailableScooters_whenSomeScootersAvailable_shouldReturnListOfAvailableScooters() {
        // Arrange
        // scooter1 is available, scooter2 is not.
        // The repository method findByIsAvailable(true) should only return scooter1.
        when(scooterRepository.findByIsAvailable(true)).thenReturn(Arrays.asList(scooter1));

        // Act
        List<Scooter> result = scooterService.getAvailableScooters();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(scooter1.getScooterCode(), result.get(0).getScooterCode());
        assertTrue(result.get(0).getAvailable());
        verify(scooterRepository, times(1)).findByIsAvailable(true);
    }

    // --- 测试 getScooterById ---
    @Test
    void getScooterById_whenScooterExists_shouldReturnScooter() {
        // Arrange
        when(scooterRepository.findById(1L)).thenReturn(Optional.of(scooter1));

        // Act
        Scooter result = scooterService.getScooterById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(scooter1.getId(), result.getId());
        assertEquals(scooter1.getScooterCode(), result.getScooterCode());
        verify(scooterRepository, times(1)).findById(1L);
    }

    @Test
    void getScooterById_whenScooterDoesNotExist_shouldReturnNull() {
        // Arrange
        when(scooterRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Scooter result = scooterService.getScooterById(99L);

        // Assert
        assertNull(result);
        verify(scooterRepository, times(1)).findById(99L);
    }

    // --- 测试 addScooter ---
    @Test
    void addScooter_shouldSaveAndReturnScooter() {
        // Arrange
        Scooter newScooter = new Scooter();
        newScooter.setScooterCode("S003");
        newScooter.setModel("Model Z");
        newScooter.setBatteryLevel(new BigDecimal("100.0"));
        newScooter.setLocation("Warehouse");
        newScooter.setAvailable(true);

        // Mock the save operation to return the scooter passed to it, simulating ID generation if needed
        when(scooterRepository.save(any(Scooter.class))).thenAnswer(invocation -> {
            Scooter scooterToSave = invocation.getArgument(0);
            if (scooterToSave.getId() == null) { // Simulate ID generation by repository
                scooterToSave.setId(3L); 
            }
            return scooterToSave;
        });

        // Act
        Scooter savedScooter = scooterService.addScooter(newScooter);

        // Assert
        assertNotNull(savedScooter);
        assertEquals(3L, savedScooter.getId()); // Check if ID was set
        assertEquals(newScooter.getScooterCode(), savedScooter.getScooterCode());
        assertEquals(newScooter.getModel(), savedScooter.getModel());
        assertEquals(newScooter.getBatteryLevel(), savedScooter.getBatteryLevel());
        assertEquals(newScooter.getLocation(), savedScooter.getLocation());
        assertTrue(savedScooter.getAvailable());

        verify(scooterRepository, times(1)).save(newScooter); // Verify save was called with the newScooter object
    }

    // --- 测试 getAllScooters ---
    @Test
    void getAllScooters_whenScootersExist_shouldReturnScooterList() {
        // Arrange
        List<Scooter> allScooters = Arrays.asList(scooter1, scooter2);
        when(scooterRepository.findAll()).thenReturn(allScooters);

        // Act
        List<Scooter> result = scooterService.getAllScooters();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(scooter1.getScooterCode(), result.get(0).getScooterCode());
        assertEquals(scooter2.getScooterCode(), result.get(1).getScooterCode());
        verify(scooterRepository, times(1)).findAll();
    }

    @Test
    void getAllScooters_whenNoScootersExist_shouldReturnEmptyList() {
        // Arrange
        when(scooterRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Scooter> result = scooterService.getAllScooters();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(scooterRepository, times(1)).findAll();
    }

    // --- 测试 updateScooter ---
    @Test
    void updateScooter_whenScooterExists_shouldUpdateAndReturnScooter() {
        // Arrange
        Long scooterIdToUpdate = 1L;
        Scooter detailsToUpdate = new Scooter();
        detailsToUpdate.setScooterCode("S001_UPDATED");
        detailsToUpdate.setModel("Model X Enhanced");
        detailsToUpdate.setBatteryLevel(new BigDecimal("95.5"));
        detailsToUpdate.setLocation("Times Square");
        detailsToUpdate.setAvailable(false);
        // detailsToUpdate.setLastMaintenanceDate(LocalDate.now()); // Assuming Scooter model has this field

        // scooter1 is the existing scooter that will be found by findById
        when(scooterRepository.findById(scooterIdToUpdate)).thenReturn(Optional.of(scooter1));
        // When save is called, it should be with scooter1 (now updated) and it should return the updated scooter1
        when(scooterRepository.save(any(Scooter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Scooter updatedScooter = scooterService.updateScooter(scooterIdToUpdate, detailsToUpdate);

        // Assert
        assertNotNull(updatedScooter);
        assertEquals(scooterIdToUpdate, updatedScooter.getId());
        assertEquals(detailsToUpdate.getScooterCode(), updatedScooter.getScooterCode());
        assertEquals(detailsToUpdate.getModel(), updatedScooter.getModel());
        assertEquals(detailsToUpdate.getBatteryLevel(), updatedScooter.getBatteryLevel());
        assertEquals(detailsToUpdate.getLocation(), updatedScooter.getLocation());
        assertEquals(detailsToUpdate.getAvailable(), updatedScooter.getAvailable());
        // if (detailsToUpdate.getLastMaintenanceDate() != null) {
        //     assertEquals(detailsToUpdate.getLastMaintenanceDate(), updatedScooter.getLastMaintenanceDate());
        // }

        verify(scooterRepository, times(1)).findById(scooterIdToUpdate);
        // Verify that the save method was called with the scooter object that was originally retrieved and then modified.
        // We can capture the argument to save and check its state if needed, but for now checking the returned object is often sufficient.
        ArgumentCaptor<Scooter> scooterCaptor = ArgumentCaptor.forClass(Scooter.class);
        verify(scooterRepository, times(1)).save(scooterCaptor.capture());
        Scooter savedScooterArg = scooterCaptor.getValue();
        assertEquals(scooterIdToUpdate, savedScooterArg.getId()); // Ensure it's the same scooter entity
        assertEquals("S001_UPDATED", savedScooterArg.getScooterCode()); // Check a modified field

    }

    @Test
    void updateScooter_whenScooterDoesNotExist_shouldReturnNull() {
        // Arrange
        Long nonExistentScooterId = 99L;
        Scooter detailsToUpdate = new Scooter();
        detailsToUpdate.setScooterCode("S999");
        detailsToUpdate.setModel("Ghost Model");

        when(scooterRepository.findById(nonExistentScooterId)).thenReturn(Optional.empty());

        // Act
        Scooter result = scooterService.updateScooter(nonExistentScooterId, detailsToUpdate);

        // Assert
        assertNull(result);
        verify(scooterRepository, times(1)).findById(nonExistentScooterId);
        verify(scooterRepository, never()).save(any(Scooter.class)); // Ensure save is not called
    }

    // --- 测试 updateScooterStatus ---
    @Test
    void updateScooterStatus_whenScooterExistsAndStatusIsAvailable_shouldSetAvailableTrue() {
        // Arrange
        Long scooterId = 2L; // scooter2 is initially not available
        when(scooterRepository.findById(scooterId)).thenReturn(Optional.of(scooter2)); 
        when(scooterRepository.save(any(Scooter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Scooter updatedScooter = scooterService.updateScooterStatus(scooterId, "Available");

        // Assert
        assertNotNull(updatedScooter);
        assertTrue(updatedScooter.getAvailable());
        verify(scooterRepository, times(1)).findById(scooterId);
        verify(scooterRepository, times(1)).save(scooter2); 
    }

    @Test
    void updateScooterStatus_whenScooterExistsAndStatusIsAvailableLowerCase_shouldSetAvailableTrue() {
        // Arrange
        Long scooterId = 2L; // scooter2 is initially not available
        when(scooterRepository.findById(scooterId)).thenReturn(Optional.of(scooter2));
        when(scooterRepository.save(any(Scooter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Scooter updatedScooter = scooterService.updateScooterStatus(scooterId, "available");

        // Assert
        assertNotNull(updatedScooter);
        assertTrue(updatedScooter.getAvailable());
    }

    @Test
    void updateScooterStatus_whenScooterExistsAndStatusIsNotAvailable_shouldSetAvailableFalse() {
        // Arrange
        Long scooterId = 1L; // scooter1 is initially available
        when(scooterRepository.findById(scooterId)).thenReturn(Optional.of(scooter1));
        when(scooterRepository.save(any(Scooter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Scooter updatedScooter = scooterService.updateScooterStatus(scooterId, "InUse");

        // Assert
        assertNotNull(updatedScooter);
        assertFalse(updatedScooter.getAvailable());
        verify(scooterRepository, times(1)).findById(scooterId);
        verify(scooterRepository, times(1)).save(scooter1);
    }

    @Test
    void updateScooterStatus_whenScooterDoesNotExist_shouldReturnNull() {
        // Arrange
        Long nonExistentScooterId = 99L;
        when(scooterRepository.findById(nonExistentScooterId)).thenReturn(Optional.empty());

        // Act
        Scooter result = scooterService.updateScooterStatus(nonExistentScooterId, "Available");

        // Assert
        assertNull(result);
        verify(scooterRepository, times(1)).findById(nonExistentScooterId);
        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    // --- 测试 deleteScooter ---
    @Test
    void deleteScooter_shouldCallRepositoryDeleteById() {
        // Arrange
        Long scooterIdToDelete = 1L;
        // No need to mock findById or anything else for deleteById unless the service method checks existence first.
        // The service method directly calls deleteById.
        doNothing().when(scooterRepository).deleteById(scooterIdToDelete); // Mocking void method

        // Act
        scooterService.deleteScooter(scooterIdToDelete);

        // Assert
        verify(scooterRepository, times(1)).deleteById(scooterIdToDelete);
    }

    @Test
    void deleteScooter_whenIdIsNotNull_shouldCallRepositoryDeleteById() {
        // Arrange
        Long scooterIdToDelete = 2L;
        doNothing().when(scooterRepository).deleteById(eq(scooterIdToDelete));

        // Act
        scooterService.deleteScooter(scooterIdToDelete);

        // Assert
        verify(scooterRepository, times(1)).deleteById(scooterIdToDelete);
    }

    // It's generally good practice to also test what happens if a null ID is passed,
    // though the current service method doesn't have explicit null checks for the ID before calling repository.
    // The repository might throw an IllegalArgumentException for a null ID.
    // For this exercise, we'll assume valid (non-null) IDs are passed based on typical controller/request validation.
    // If specific null handling is added to the service, a test for that should be included.
} 