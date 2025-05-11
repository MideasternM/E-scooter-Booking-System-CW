package org.example.escooter_booking_system.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.escooter_booking_system.dto.RentalStoreDTO;
import org.example.escooter_booking_system.model.RentalStore;
import org.example.escooter_booking_system.repository.RentalStoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalStoreServiceImplTest {

    @Mock
    private RentalStoreRepository rentalStoreRepository;

    @InjectMocks
    private RentalStoreServiceImpl rentalStoreService;

    private RentalStoreDTO rentalStoreDTO;
    private RentalStore rentalStore;

    @BeforeEach
    void setUp() {
        rentalStoreDTO = new RentalStoreDTO();
        rentalStoreDTO.setName("Main Street Store");
        rentalStoreDTO.setCode("MSS001");
        rentalStoreDTO.setLatitude(34.0522);
        rentalStoreDTO.setLongitude(-118.2437);
        rentalStoreDTO.setAddress("123 Main St, Anytown");
        rentalStoreDTO.setOpeningHours("9AM-9PM");
        rentalStoreDTO.setContactPhone("555-1234");
        rentalStoreDTO.setDescription("Our flagship store");
        rentalStoreDTO.setImageUrl("http://example.com/store.jpg");
        rentalStoreDTO.setServicesOffered(new ArrayList<>(Arrays.asList(RentalStore.StoreService.RENTAL, RentalStore.StoreService.REPAIR)));

        rentalStore = new RentalStore();
        rentalStore.setId(1L);
        rentalStore.setName(rentalStoreDTO.getName());
        rentalStore.setCode(rentalStoreDTO.getCode());
        rentalStore.setStatus(RentalStore.StoreStatus.OPERATIONAL);
        rentalStore.getAvailableScooters().add("SC001");
    }

    @Test
    void createRentalStore_whenCodeIsUnique_shouldSaveAndReturnStore() {
        // Arrange
        when(rentalStoreRepository.findByCode(rentalStoreDTO.getCode())).thenReturn(Optional.empty());
        when(rentalStoreRepository.save(any(RentalStore.class))).thenAnswer(invocation -> {
            RentalStore storeToSave = invocation.getArgument(0);
            storeToSave.setId(1L); // Simulate ID generation on save
            return storeToSave;
        });

        // Act
        RentalStore createdStore = rentalStoreService.createRentalStore(rentalStoreDTO);

        // Assert
        assertNotNull(createdStore);
        assertEquals(1L, createdStore.getId());
        assertEquals(rentalStoreDTO.getName(), createdStore.getName());
        assertEquals(rentalStoreDTO.getCode(), createdStore.getCode());
        assertEquals(RentalStore.StoreStatus.OPERATIONAL, createdStore.getStatus());
        assertTrue(createdStore.getServicesOffered().contains(RentalStore.StoreService.RENTAL));
        assertTrue(createdStore.getServicesOffered().contains(RentalStore.StoreService.REPAIR));

        ArgumentCaptor<RentalStore> storeCaptor = ArgumentCaptor.forClass(RentalStore.class);
        verify(rentalStoreRepository).save(storeCaptor.capture());
        RentalStore capturedStore = storeCaptor.getValue();
        assertEquals(rentalStoreDTO.getAddress(), capturedStore.getAddress());
    }

    @Test
    void createRentalStore_whenCodeIsNotUnique_shouldThrowIllegalArgumentException() {
        // Arrange
        when(rentalStoreRepository.findByCode(rentalStoreDTO.getCode())).thenReturn(Optional.of(new RentalStore())); // Simulate code already exists

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            rentalStoreService.createRentalStore(rentalStoreDTO);
        });
        assertTrue(exception.getMessage().contains("already exists"));
        verify(rentalStoreRepository, never()).save(any(RentalStore.class));
    }

    @Test
    void createRentalStore_whenServicesOfferedIsNullInDTO_shouldDefaultToRental() {
        // Arrange
        rentalStoreDTO.setServicesOffered(null);
        when(rentalStoreRepository.findByCode(rentalStoreDTO.getCode())).thenReturn(Optional.empty());
        when(rentalStoreRepository.save(any(RentalStore.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RentalStore createdStore = rentalStoreService.createRentalStore(rentalStoreDTO);

        // Assert
        assertNotNull(createdStore.getServicesOffered());
        assertEquals(1, createdStore.getServicesOffered().size());
        assertTrue(createdStore.getServicesOffered().contains(RentalStore.StoreService.RENTAL));
    }

    @Test
    void createRentalStore_whenServicesOfferedIsEmptyInDTO_shouldDefaultToRental() {
        // Arrange
        rentalStoreDTO.setServicesOffered(Collections.emptyList());
        when(rentalStoreRepository.findByCode(rentalStoreDTO.getCode())).thenReturn(Optional.empty());
        when(rentalStoreRepository.save(any(RentalStore.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RentalStore createdStore = rentalStoreService.createRentalStore(rentalStoreDTO);

        // Assert
        assertNotNull(createdStore.getServicesOffered());
        assertEquals(1, createdStore.getServicesOffered().size());
        assertTrue(createdStore.getServicesOffered().contains(RentalStore.StoreService.RENTAL));
    }

    @Test
    void getAllRentalStores_shouldReturnStoreList() {
        // Arrange
        List<RentalStore> stores = Arrays.asList(rentalStore, new RentalStore());
        when(rentalStoreRepository.findAll()).thenReturn(stores);

        // Act
        List<RentalStore> result = rentalStoreService.getAllRentalStores();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(rentalStoreRepository, times(1)).findAll();
    }

    @Test
    void getAllRentalStores_whenNoStores_shouldReturnEmptyList() {
        // Arrange
        when(rentalStoreRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<RentalStore> result = rentalStoreService.getAllRentalStores();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(rentalStoreRepository, times(1)).findAll();
    }

    @Test
    void getRentalStoreById_whenStoreExists_shouldReturnOptionalWithStore() {
        // Arrange
        Long storeId = 1L;
        when(rentalStoreRepository.findById(storeId)).thenReturn(Optional.of(rentalStore));

        // Act
        Optional<RentalStore> foundStoreOptional = rentalStoreService.getRentalStoreById(storeId);

        // Assert
        assertTrue(foundStoreOptional.isPresent());
        RentalStore foundStore = foundStoreOptional.get();
        assertEquals(storeId, foundStore.getId());
        assertEquals(rentalStore.getName(), foundStore.getName());
        verify(rentalStoreRepository, times(1)).findById(storeId);
    }

    @Test
    void getRentalStoreById_whenStoreNotExists_shouldReturnEmptyOptional() {
        // Arrange
        Long storeId = 2L;
        when(rentalStoreRepository.findById(storeId)).thenReturn(Optional.empty());

        // Act
        Optional<RentalStore> foundStoreOptional = rentalStoreService.getRentalStoreById(storeId);

        // Assert
        assertTrue(foundStoreOptional.isEmpty());
        verify(rentalStoreRepository, times(1)).findById(storeId);
    }

    @Test
    void getRentalStoreByCode_whenStoreExists_shouldReturnOptionalWithStore() {
        // Arrange
        String storeCode = "MSS001";
        rentalStore.setCode(storeCode); // Ensure rentalStore has the code we are looking for
        when(rentalStoreRepository.findByCode(storeCode)).thenReturn(Optional.of(rentalStore));

        // Act
        Optional<RentalStore> foundStoreOptional = rentalStoreService.getRentalStoreByCode(storeCode);

        // Assert
        assertTrue(foundStoreOptional.isPresent());
        RentalStore foundStore = foundStoreOptional.get();
        assertEquals(storeCode, foundStore.getCode());
        assertEquals(rentalStore.getName(), foundStore.getName());
        verify(rentalStoreRepository, times(1)).findByCode(storeCode);
    }

    @Test
    void getRentalStoreByCode_whenStoreNotExists_shouldReturnEmptyOptional() {
        // Arrange
        String storeCode = "UNKNOWN001";
        when(rentalStoreRepository.findByCode(storeCode)).thenReturn(Optional.empty());

        // Act
        Optional<RentalStore> foundStoreOptional = rentalStoreService.getRentalStoreByCode(storeCode);

        // Assert
        assertTrue(foundStoreOptional.isEmpty());
        verify(rentalStoreRepository, times(1)).findByCode(storeCode);
    }

    @Test
    void updateRentalStore_whenStoreExistsAndCodeNotConflicting_shouldUpdateAndReturnStore() {
        // Arrange
        Long storeId = 1L;
        RentalStoreDTO updatedDetailsDTO = new RentalStoreDTO();
        updatedDetailsDTO.setName("Updated Name");
        updatedDetailsDTO.setCode("MSS001_UPDATED"); // New unique code
        updatedDetailsDTO.setAddress("456 Updated St");
        updatedDetailsDTO.setLatitude(35.0000);
        updatedDetailsDTO.setLongitude(-119.0000);
        updatedDetailsDTO.setOpeningHours("8AM-8PM");
        updatedDetailsDTO.setContactPhone("555-5678");
        updatedDetailsDTO.setDescription("Updated description");
        updatedDetailsDTO.setImageUrl("http://example.com/updated_store.jpg");
        updatedDetailsDTO.setStatus(RentalStore.StoreStatus.CLOSED_TEMPORARY);
        updatedDetailsDTO.setServicesOffered(new ArrayList<>(Collections.singletonList(RentalStore.StoreService.REPAIR)));

        RentalStore existingStore = new RentalStore(); // Simulate the existing store found by ID
        existingStore.setId(storeId);
        existingStore.setName("Old Name");
        existingStore.setCode("MSS001"); // Original code
        existingStore.setStatus(RentalStore.StoreStatus.OPERATIONAL);
        existingStore.getServicesOffered().add(RentalStore.StoreService.RENTAL);

        when(rentalStoreRepository.findById(storeId)).thenReturn(Optional.of(existingStore));
        when(rentalStoreRepository.findByCode(updatedDetailsDTO.getCode())).thenReturn(Optional.empty()); // No conflict with new code
        when(rentalStoreRepository.save(any(RentalStore.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RentalStore updatedStore = rentalStoreService.updateRentalStore(storeId, updatedDetailsDTO);

        // Assert
        assertNotNull(updatedStore);
        assertEquals(storeId, updatedStore.getId());
        assertEquals(updatedDetailsDTO.getName(), updatedStore.getName());
        assertEquals(updatedDetailsDTO.getCode(), updatedStore.getCode());
        assertEquals(updatedDetailsDTO.getAddress(), updatedStore.getAddress());
        assertEquals(updatedDetailsDTO.getLatitude(), updatedStore.getLatitude());
        assertEquals(updatedDetailsDTO.getLongitude(), updatedStore.getLongitude());
        assertEquals(updatedDetailsDTO.getOpeningHours(), updatedStore.getOpeningHours());
        assertEquals(updatedDetailsDTO.getContactPhone(), updatedStore.getContactPhone());
        assertEquals(updatedDetailsDTO.getDescription(), updatedStore.getDescription());
        assertEquals(updatedDetailsDTO.getImageUrl(), updatedStore.getImageUrl());
        assertEquals(updatedDetailsDTO.getStatus(), updatedStore.getStatus());
        assertEquals(updatedDetailsDTO.getServicesOffered(), updatedStore.getServicesOffered());

        verify(rentalStoreRepository, times(1)).findById(storeId);
        verify(rentalStoreRepository, times(1)).findByCode(updatedDetailsDTO.getCode());
        verify(rentalStoreRepository, times(1)).save(existingStore);
    }

    @Test
    void updateRentalStore_whenStoreExistsAndCodeIsSame_shouldUpdateAndReturnStore() {
        // Arrange
        Long storeId = 1L;
        String originalCode = "MSS001";
        RentalStoreDTO updatedDetailsDTO = new RentalStoreDTO();
        updatedDetailsDTO.setName("Updated Name Again");
        updatedDetailsDTO.setCode(originalCode); // Code is the same
        updatedDetailsDTO.setAddress("789 Another St");

        RentalStore existingStore = new RentalStore();
        existingStore.setId(storeId);
        existingStore.setName("Old Name");
        existingStore.setCode(originalCode);

        when(rentalStoreRepository.findById(storeId)).thenReturn(Optional.of(existingStore));
        // When code is same, findByCode for the *same* code should return the *current* store for the check to pass
        when(rentalStoreRepository.findByCode(originalCode)).thenReturn(Optional.of(existingStore));
        when(rentalStoreRepository.save(any(RentalStore.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RentalStore updatedStore = rentalStoreService.updateRentalStore(storeId, updatedDetailsDTO);

        // Assert
        assertNotNull(updatedStore);
        assertEquals(updatedDetailsDTO.getName(), updatedStore.getName());
        assertEquals(originalCode, updatedStore.getCode());
        assertEquals(updatedDetailsDTO.getAddress(), updatedStore.getAddress());

        verify(rentalStoreRepository, times(1)).findById(storeId);
        verify(rentalStoreRepository, times(1)).findByCode(originalCode);
        verify(rentalStoreRepository, times(1)).save(existingStore);
    }

    @Test
    void updateRentalStore_whenStoreNotFound_shouldThrowEntityNotFoundException() {
        // Arrange
        Long storeId = 99L; // Non-existent ID
        RentalStoreDTO updatedDetailsDTO = new RentalStoreDTO();
        updatedDetailsDTO.setName("Updated Name");
        updatedDetailsDTO.setCode("NEWCODE001");

        when(rentalStoreRepository.findById(storeId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            rentalStoreService.updateRentalStore(storeId, updatedDetailsDTO);
        });
        assertEquals("Rental store with id " + storeId + " not found.", exception.getMessage());
        verify(rentalStoreRepository, times(1)).findById(storeId);
        verify(rentalStoreRepository, never()).findByCode(anyString());
        verify(rentalStoreRepository, never()).save(any(RentalStore.class));
    }

    @Test
    void updateRentalStore_whenNewCodeConflictsWithAnotherStore_shouldThrowIllegalArgumentException() {
        // Arrange
        Long storeIdToUpdate = 1L;
        String conflictingCode = "CONFLICT002";

        RentalStoreDTO updatedDetailsDTO = new RentalStoreDTO();
        updatedDetailsDTO.setName("Attempt Update Name");
        updatedDetailsDTO.setCode(conflictingCode);

        RentalStore storeToUpdate = new RentalStore();
        storeToUpdate.setId(storeIdToUpdate);
        storeToUpdate.setCode("ORIGINAL001");

        RentalStore anotherStoreWithConflictingCode = new RentalStore();
        anotherStoreWithConflictingCode.setId(2L); // Different ID
        anotherStoreWithConflictingCode.setCode(conflictingCode);

        when(rentalStoreRepository.findById(storeIdToUpdate)).thenReturn(Optional.of(storeToUpdate));
        when(rentalStoreRepository.findByCode(conflictingCode)).thenReturn(Optional.of(anotherStoreWithConflictingCode));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            rentalStoreService.updateRentalStore(storeIdToUpdate, updatedDetailsDTO);
        });
        assertEquals("Another rental store with code " + conflictingCode + " already exists.", exception.getMessage());
        verify(rentalStoreRepository, times(1)).findById(storeIdToUpdate);
        verify(rentalStoreRepository, times(1)).findByCode(conflictingCode);
        verify(rentalStoreRepository, never()).save(any(RentalStore.class));
    }
} 