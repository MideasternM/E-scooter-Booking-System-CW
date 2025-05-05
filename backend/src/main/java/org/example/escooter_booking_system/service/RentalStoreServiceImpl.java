package org.example.escooter_booking_system.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.escooter_booking_system.dto.RentalStoreDTO;
import org.example.escooter_booking_system.model.RentalStore;
import org.example.escooter_booking_system.repository.RentalStoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // Lombok annotation for constructor injection
public class RentalStoreServiceImpl implements RentalStoreService {

    private final RentalStoreRepository rentalStoreRepository;
    // Assuming you might need ScooterRepository later for validation
    // private final ScooterRepository scooterRepository;

    @Override
    @Transactional
    public RentalStore createRentalStore(RentalStoreDTO storeDTO) {
        // Basic validation: Check if code already exists
        if (rentalStoreRepository.findByCode(storeDTO.getCode()).isPresent()) {
            throw new IllegalArgumentException("Rental store with code " + storeDTO.getCode() + " already exists.");
        }

        RentalStore store = new RentalStore();
        // Map DTO fields to Entity fields
        store.setName(storeDTO.getName());
        store.setCode(storeDTO.getCode());
        store.setLatitude(storeDTO.getLatitude());
        store.setLongitude(storeDTO.getLongitude());
        store.setAddress(storeDTO.getAddress());
        store.setOpeningHours(storeDTO.getOpeningHours());
        store.setContactPhone(storeDTO.getContactPhone());
        store.setDescription(storeDTO.getDescription());
        store.setImageUrl(storeDTO.getImageUrl());
        // Set default status or get from DTO if added
        store.setStatus(RentalStore.StoreStatus.OPERATIONAL);
        // Set provided services or default to RENTAL
        if (storeDTO.getServicesOffered() != null && !storeDTO.getServicesOffered().isEmpty()) {
            store.setServicesOffered(storeDTO.getServicesOffered());
        } else {
            store.getServicesOffered().clear(); // Ensure list is cleared if null/empty
            store.getServicesOffered().add(RentalStore.StoreService.RENTAL);
        }

        return rentalStoreRepository.save(store);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalStore> getAllRentalStores() {
        return rentalStoreRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RentalStore> getRentalStoreById(Long id) {
        return rentalStoreRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RentalStore> getRentalStoreByCode(String code) {
        return rentalStoreRepository.findByCode(code);
    }

    @Override
    @Transactional
    public RentalStore updateRentalStore(Long id, RentalStoreDTO storeDTO) {
        RentalStore existingStore = rentalStoreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rental store with id " + id + " not found."));

        // Check if the updated code conflicts with another existing store
        Optional<RentalStore> storeWithSameCode = rentalStoreRepository.findByCode(storeDTO.getCode());
        if (storeWithSameCode.isPresent() && !storeWithSameCode.get().getId().equals(id)) {
            throw new IllegalArgumentException(
                    "Another rental store with code " + storeDTO.getCode() + " already exists.");
        }

        // Update fields from DTO
        existingStore.setName(storeDTO.getName());
        existingStore.setCode(storeDTO.getCode());
        existingStore.setLatitude(storeDTO.getLatitude());
        existingStore.setLongitude(storeDTO.getLongitude());
        existingStore.setAddress(storeDTO.getAddress());
        existingStore.setOpeningHours(storeDTO.getOpeningHours());
        existingStore.setContactPhone(storeDTO.getContactPhone());
        existingStore.setDescription(storeDTO.getDescription());
        existingStore.setImageUrl(storeDTO.getImageUrl());

        // Update status if provided in the DTO
        if (storeDTO.getStatus() != null) {
            existingStore.setStatus(storeDTO.getStatus());
        }

        if (storeDTO.getServicesOffered() != null) { // Allow clearing services by sending empty list
            existingStore.setServicesOffered(storeDTO.getServicesOffered());
        }
        // Note: Status and available scooters are typically updated via separate
        // methods/endpoints

        return rentalStoreRepository.save(existingStore);
    }

    @Override
    @Transactional
    public void deleteRentalStore(Long id) {
        if (!rentalStoreRepository.existsById(id)) {
            throw new EntityNotFoundException("Rental store with id " + id + " not found.");
        }
        // Instead of deleting, you might want to change status:
        // RentalStore store = rentalStoreRepository.findById(id).get();
        // store.setStatus(RentalStore.StoreStatus.CLOSED_PERMANENT);
        // rentalStoreRepository.save(store);

        // Actual deletion:
        rentalStoreRepository.deleteById(id);
    }

    @Override
    @Transactional
    public RentalStore addScooterToStore(Long storeId, String scooterCode) {
        RentalStore store = rentalStoreRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Rental store with id " + storeId + " not found."));

        // Optional: Check if scooter exists in ScooterRepository
        // Optional<Scooter> scooter = scooterRepository.findByCode(scooterCode);
        // if (scooter.isEmpty()) {
        // throw new EntityNotFoundException("Scooter with code " + scooterCode + " not
        // found.");
        // }

        // Check if scooter is already in the list
        if (!store.getAvailableScooters().contains(scooterCode)) {
            store.getAvailableScooters().add(scooterCode);
            return rentalStoreRepository.save(store);
        } else {
            // Optionally throw error or just return the store as is
            throw new IllegalArgumentException("Scooter " + scooterCode + " is already listed in store " + storeId);
            // return store;
        }
    }

    @Override
    @Transactional
    public RentalStore removeScooterFromStore(Long storeId, String scooterCode) {
        RentalStore store = rentalStoreRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Rental store with id " + storeId + " not found."));

        boolean removed = store.getAvailableScooters().remove(scooterCode);

        if (removed) {
            return rentalStoreRepository.save(store);
        } else {
            // Optionally throw error or just return the store as is
            throw new EntityNotFoundException("Scooter " + scooterCode + " not found in store " + storeId);
            // return store;
        }
    }

    // Implementation for findStoresNear would require geospatial query capabilities
    // (e.g., using database functions or a library like Hibernate Spatial)
}