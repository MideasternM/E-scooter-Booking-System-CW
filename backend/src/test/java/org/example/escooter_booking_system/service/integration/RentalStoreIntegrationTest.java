package org.example.escooter_booking_system.service.integration;

import org.example.escooter_booking_system.config.TestApplication;
import org.example.escooter_booking_system.dto.RentalStoreDTO;
import org.example.escooter_booking_system.model.RentalStore;
import org.example.escooter_booking_system.repository.RentalStoreRepository;
import org.example.escooter_booking_system.service.RentalStoreService;
import org.example.escooter_booking_system.service.ScooterService;
import org.example.escooter_booking_system.model.Scooter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb_rental_store;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.show-sql=true",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.h2.console.enabled=true",
    "spring.h2.console.path=/h2-console-rental-store"
})
@Transactional
public class RentalStoreIntegrationTest {

    @Autowired
    private RentalStoreService rentalStoreService;

    @Autowired
    private RentalStoreRepository rentalStoreRepository; // For direct DB verification if needed

    @Autowired
    private ScooterService scooterService;

    private RentalStoreDTO createRentalStoreDTOSample(String code, String name) {
        RentalStoreDTO dto = new RentalStoreDTO();
        dto.setCode(code);
        dto.setName(name);
        dto.setAddress("123 Test Street, Testville");
        dto.setLatitude(34.0522);
        dto.setLongitude(-118.2437);
        dto.setStatus(RentalStore.StoreStatus.OPERATIONAL);
        dto.setOpeningHours("09:00-20:00");
        dto.setContactPhone("555-1234");
        return dto;
    }

    @Test
    void testCreateAndGetRentalStore() {
        // 1. Prepare DTO
        RentalStoreDTO storeDTO = createRentalStoreDTOSample("STORE001", "Main Street Store");

        // 2. Create RentalStore
        RentalStore createdStore = rentalStoreService.createRentalStore(storeDTO);
        assertNotNull(createdStore, "Created store should not be null.");
        assertNotNull(createdStore.getId(), "Created store ID should not be null.");
        assertEquals(storeDTO.getName(), createdStore.getName());
        assertEquals(storeDTO.getCode(), createdStore.getCode());
        assertEquals(storeDTO.getAddress(), createdStore.getAddress());
        assertEquals(storeDTO.getLatitude(), createdStore.getLatitude());
        assertEquals(storeDTO.getLongitude(), createdStore.getLongitude());
        assertEquals(storeDTO.getStatus(), createdStore.getStatus());
        assertEquals(storeDTO.getOpeningHours(), createdStore.getOpeningHours());
        assertEquals(storeDTO.getContactPhone(), createdStore.getContactPhone());

        // 3. Get by ID
        Optional<RentalStore> foundByIdOpt = rentalStoreService.getRentalStoreById(createdStore.getId());
        assertTrue(foundByIdOpt.isPresent(), "Store should be found by ID.");
        RentalStore foundByIdStore = foundByIdOpt.get();
        assertEquals(createdStore.getName(), foundByIdStore.getName());

        // 4. Get by Code
        Optional<RentalStore> foundByCodeOpt = rentalStoreService.getRentalStoreByCode(storeDTO.getCode());
        assertTrue(foundByCodeOpt.isPresent(), "Store should be found by code.");
        RentalStore foundByCodeStore = foundByCodeOpt.get();
        assertEquals(createdStore.getName(), foundByCodeStore.getName());

        // 5. Test get non-existent store by ID
        Optional<RentalStore> notFoundByIdOpt = rentalStoreService.getRentalStoreById(9999L);
        assertFalse(notFoundByIdOpt.isPresent(), "Store should not be found with a non-existent ID.");

        // 6. Test get non-existent store by Code
        Optional<RentalStore> notFoundByCodeOpt = rentalStoreService.getRentalStoreByCode("NONEXISTENT_CODE");
        assertFalse(notFoundByCodeOpt.isPresent(), "Store should not be found with a non-existent code.");
    }

    @Test
    void testUpdateRentalStore() {
        // 1. Create an initial store
        RentalStoreDTO initialDTO = createRentalStoreDTOSample("STORE_UPDATE_001", "Updatable Store");
        RentalStore createdStore = rentalStoreService.createRentalStore(initialDTO);
        assertNotNull(createdStore, "Created store for update should not be null.");
        Long storeId = createdStore.getId();

        // 2. Prepare updated DTO
        RentalStoreDTO updatedDTO = new RentalStoreDTO();
        updatedDTO.setCode("STORE_UPDATE_001"); // Code might not be updatable in some systems, or needs care
        updatedDTO.setName("Store Updated Name");
        updatedDTO.setAddress("456 Updated Ave, Testville");
        updatedDTO.setLatitude(35.1234);
        updatedDTO.setLongitude(-119.5678);
        updatedDTO.setStatus(RentalStore.StoreStatus.CLOSED_TEMPORARY);
        updatedDTO.setOpeningHours("10:00-17:00");
        updatedDTO.setContactPhone("555-8765");
        updatedDTO.setDescription("Store is temporarily closed for renovation.");

        // 3. Update the store
        RentalStore updatedStore = rentalStoreService.updateRentalStore(storeId, updatedDTO);
        assertNotNull(updatedStore, "Updated store should not be null.");

        // 4. Verify the returned updated store details
        assertEquals(storeId, updatedStore.getId());
        assertEquals(updatedDTO.getName(), updatedStore.getName());
        assertEquals(updatedDTO.getAddress(), updatedStore.getAddress());
        assertEquals(updatedDTO.getStatus(), updatedStore.getStatus());
        assertEquals(updatedDTO.getContactPhone(), updatedStore.getContactPhone());
        assertEquals(updatedDTO.getDescription(), updatedStore.getDescription());

        // 5. Fetch from DB again and verify to ensure persistence
        RentalStore fetchedStore = rentalStoreService.getRentalStoreById(storeId).orElse(null);
        assertNotNull(fetchedStore, "Fetched store after update should not be null.");
        assertEquals(updatedDTO.getName(), fetchedStore.getName(), "Name should be updated in DB.");
        assertEquals(updatedDTO.getAddress(), fetchedStore.getAddress(), "Address should be updated in DB.");
        assertEquals(updatedDTO.getStatus(), fetchedStore.getStatus(), "Status should be updated in DB.");
    }

    @Test
    void testDeleteRentalStore() {
        // 1. Create a store to be deleted
        RentalStoreDTO dtoToDelete = createRentalStoreDTOSample("STORE_DELETE_001", "Deletable Store");
        RentalStore createdStore = rentalStoreService.createRentalStore(dtoToDelete);
        assertNotNull(createdStore, "Created store for deletion should not be null.");
        Long storeId = createdStore.getId();

        // 2. Delete the store
        rentalStoreService.deleteRentalStore(storeId);

        // 3. Verify it's deleted
        Optional<RentalStore> deletedStoreOpt = rentalStoreService.getRentalStoreById(storeId);
        assertFalse(deletedStoreOpt.isPresent(), "Store should not be found by ID after deletion.");

        // Verify with repository as well for a more direct check
        assertFalse(rentalStoreRepository.findById(storeId).isPresent(), "Store should not exist in repository after deletion.");
    }

    private Scooter createAndSaveScooterSample(String scooterCode, boolean isAvailable) {
        Scooter scooter = new Scooter();
        scooter.setScooterCode(scooterCode);
        scooter.setModel("IntegrationTest ScooterModel");
        scooter.setBatteryLevel(new java.math.BigDecimal("99.9"));
        scooter.setLocation("Test Scooter Location");
        scooter.setAvailable(isAvailable);
        scooter.setLastMaintenanceDate(new java.util.Date());
        return scooterService.addScooter(scooter);
    }

    @Test
    void testAddAndRemoveScooterFromStore() {
        // 1. Create a store
        RentalStoreDTO storeDTO = createRentalStoreDTOSample("STORE_WITH_SCOOTERS", "Scooter Hub");
        RentalStore store = rentalStoreService.createRentalStore(storeDTO);
        assertNotNull(store, "Store should be created.");
        Long storeId = store.getId();

        // 2. Create a scooter
        Scooter scooter = createAndSaveScooterSample("SCOOTER_FOR_STORE_001", true);
        assertNotNull(scooter, "Scooter should be created.");
        String scooterCode = scooter.getScooterCode();

        // 3. Add scooter to store
        RentalStore storeWithScooter = rentalStoreService.addScooterToStore(storeId, scooterCode);
        assertNotNull(storeWithScooter, "Store after adding scooter should not be null.");
        assertTrue(storeWithScooter.getAvailableScooters().contains(scooterCode), 
                   "Store's available scooters should contain the added scooter code.");

        // Verify by fetching again (more robust check)
        RentalStore fetchedStoreAfterAdd = rentalStoreService.getRentalStoreById(storeId).orElse(null);
        assertNotNull(fetchedStoreAfterAdd, "Fetched store after adding scooter should not be null.");
        assertTrue(fetchedStoreAfterAdd.getAvailableScooters().contains(scooterCode),
                   "Fetched store's available scooters should contain the added scooter code.");

        // 4. Remove scooter from store
        RentalStore storeWithoutScooter = rentalStoreService.removeScooterFromStore(storeId, scooterCode);
        assertNotNull(storeWithoutScooter, "Store after removing scooter should not be null.");
        assertFalse(storeWithoutScooter.getAvailableScooters().contains(scooterCode),
                    "Store's available scooters should not contain the removed scooter code.");
        
        // Verify by fetching again
        RentalStore fetchedStoreAfterRemove = rentalStoreService.getRentalStoreById(storeId).orElse(null);
        assertNotNull(fetchedStoreAfterRemove, "Fetched store after removing scooter should not be null.");
        assertFalse(fetchedStoreAfterRemove.getAvailableScooters().contains(scooterCode),
                    "Fetched store's available scooters should not contain the removed scooter code.");
    }
} 