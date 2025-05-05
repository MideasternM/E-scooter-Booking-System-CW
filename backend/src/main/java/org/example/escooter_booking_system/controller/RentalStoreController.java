package org.example.escooter_booking_system.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.escooter_booking_system.dto.RentalStoreDTO;
import org.example.escooter_booking_system.model.RentalStore;
import org.example.escooter_booking_system.service.RentalStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map; // For scooter code request body

@RestController
@RequestMapping("/api/rental-stores")
@RequiredArgsConstructor
public class RentalStoreController {

    private final RentalStoreService rentalStoreService;

    // POST /api/rental-stores - Create a new rental store
    @PostMapping
    public ResponseEntity<RentalStore> createRentalStore(@Valid @RequestBody RentalStoreDTO storeDTO) {
        try {
            RentalStore createdStore = rentalStoreService.createRentalStore(storeDTO);
            return new ResponseEntity<>(createdStore, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // GET /api/rental-stores - Get all rental stores
    @GetMapping
    public ResponseEntity<List<RentalStore>> getAllRentalStores() {
        List<RentalStore> stores = rentalStoreService.getAllRentalStores();
        return ResponseEntity.ok(stores);
    }

    // GET /api/rental-stores/{id} - Get a rental store by ID
    @GetMapping("/{id}")
    public ResponseEntity<RentalStore> getRentalStoreById(@PathVariable Long id) {
        return rentalStoreService.getRentalStoreById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental store not found"));
    }

    // GET /api/rental-stores/by-code/{code} - Get a rental store by code
    @GetMapping("/by-code/{code}")
    public ResponseEntity<RentalStore> getRentalStoreByCode(@PathVariable String code) {
        return rentalStoreService.getRentalStoreByCode(code)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental store not found"));
    }

    // PUT /api/rental-stores/{id} - Update a rental store
    @PutMapping("/{id}")
    public ResponseEntity<RentalStore> updateRentalStore(@PathVariable Long id,
            @Valid @RequestBody RentalStoreDTO storeDTO) {
        try {
            RentalStore updatedStore = rentalStoreService.updateRentalStore(id, storeDTO);
            return ResponseEntity.ok(updatedStore);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // DELETE /api/rental-stores/{id} - Delete a rental store
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRentalStore(@PathVariable Long id) {
        try {
            rentalStoreService.deleteRentalStore(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // --- Scooter Management Endpoints ---

    // POST /api/rental-stores/{id}/scooters - Add a scooter to the store
    @PostMapping("/{id}/scooters")
    public ResponseEntity<RentalStore> addScooterToStore(@PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        String scooterCode = requestBody.get("scooterCode");
        if (scooterCode == null || scooterCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scooterCode is required in the request body.");
        }
        try {
            RentalStore updatedStore = rentalStoreService.addScooterToStore(id, scooterCode);
            return ResponseEntity.ok(updatedStore);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // DELETE /api/rental-stores/{id}/scooters/{scooterCode} - Remove a scooter from
    // the store
    @DeleteMapping("/{id}/scooters/{scooterCode}")
    public ResponseEntity<RentalStore> removeScooterFromStore(@PathVariable Long id, @PathVariable String scooterCode) {
        try {
            RentalStore updatedStore = rentalStoreService.removeScooterFromStore(id, scooterCode);
            return ResponseEntity.ok(updatedStore);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}