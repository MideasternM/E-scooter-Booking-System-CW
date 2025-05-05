package org.example.escooter_booking_system.repository;

import org.example.escooter_booking_system.model.RentalStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentalStoreRepository extends JpaRepository<RentalStore, Long> {

    // Find a store by its unique code
    Optional<RentalStore> findByCode(String code);

    // You can add more custom query methods here if needed, for example:
    // List<RentalStore> findByStatus(RentalStore.StoreStatus status);
    // List<RentalStore> findByAddressContainingIgnoreCase(String addressKeyword);
}