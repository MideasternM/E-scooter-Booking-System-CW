package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.RentalStore;
import org.example.escooter_booking_system.dto.RentalStoreDTO; // Assuming a DTO will be created
import java.util.List;
import java.util.Optional;

public interface RentalStoreService {

    // Create a new rental store
    RentalStore createRentalStore(RentalStoreDTO storeDTO);

    // Get all rental stores
    List<RentalStore> getAllRentalStores();

    // Get a rental store by its ID
    Optional<RentalStore> getRentalStoreById(Long id);

    // Get a rental store by its code
    Optional<RentalStore> getRentalStoreByCode(String code);

    // Update an existing rental store
    RentalStore updateRentalStore(Long id, RentalStoreDTO storeDTO);

    // Delete a rental store (or mark as inactive)
    void deleteRentalStore(Long id);

    // Add a scooter to a store's available list
    RentalStore addScooterToStore(Long storeId, String scooterCode);

    // Remove a scooter from a store's available list
    RentalStore removeScooterFromStore(Long storeId, String scooterCode);

    // Find stores near a given location (Example)
    // List<RentalStore> findStoresNear(double latitude, double longitude, double
    // radiusKm);

}