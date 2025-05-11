package org.example.escooter_booking_system.service.integration;

import org.example.escooter_booking_system.config.TestApplication;
import org.example.escooter_booking_system.model.Scooter;
import org.example.escooter_booking_system.repository.ScooterRepository;
import org.example.escooter_booking_system.service.ScooterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb_scooter_admin;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", // Use a different DB name for isolation if needed
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.show-sql=true",
    "spring.jpa.hibernate.ddl-auto=create-drop", // Ensure ddl-auto is create-drop for tests
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.h2.console.enabled=true",
    "spring.h2.console.path=/h2-console-scooter-admin"
})
@Transactional
public class ScooterAdminIntegrationTest {

    @Autowired
    private ScooterService scooterService;

    @Autowired
    private ScooterRepository scooterRepository; // For direct DB verification if needed

    @Test
    void testAdminUpdatesScooterDetails() {
        // 1. Create and save a scooter
        Scooter initialScooter = new Scooter();
        initialScooter.setScooterCode("ADMIN_S001");
        initialScooter.setModel("Alpha Model");
        initialScooter.setBatteryLevel(new BigDecimal("95.0"));
        initialScooter.setLocation("Central Depot");
        initialScooter.setAvailable(true);
        initialScooter.setLastMaintenanceDate(new Date());

        Scooter savedScooter = scooterService.addScooter(initialScooter);
        assertNotNull(savedScooter, "Saved scooter should not be null.");
        assertNotNull(savedScooter.getId(), "Saved scooter ID should not be null.");

        // 2. Prepare updated details
        Scooter scooterToUpdate = new Scooter(); // Create a new DTO-like object for update
        scooterToUpdate.setScooterCode(savedScooter.getScooterCode()); // Usually code is not updatable or handled carefully
        scooterToUpdate.setModel("Alpha Model V2"); // Updated model
        scooterToUpdate.setBatteryLevel(new BigDecimal("92.5")); // Updated battery
        scooterToUpdate.setLocation("East Wing Depot"); // Updated location
        scooterToUpdate.setAvailable(savedScooter.getAvailable()); // Keep availability same for this test
        scooterToUpdate.setLastMaintenanceDate(savedScooter.getLastMaintenanceDate()); // Keep maintenance date same

        // 3. Update the scooter
        Scooter updatedScooter = scooterService.updateScooter(savedScooter.getId(), scooterToUpdate);
        assertNotNull(updatedScooter, "Updated scooter should not be null.");

        // 4. Fetch and verify
        Scooter fetchedScooter = scooterService.getScooterById(savedScooter.getId());
        assertNotNull(fetchedScooter, "Fetched scooter after update should not be null.");
        assertEquals(savedScooter.getId(), fetchedScooter.getId(), "IDs should match.");
        assertEquals("Alpha Model V2", fetchedScooter.getModel(), "Model should be updated.");
        assertEquals("East Wing Depot", fetchedScooter.getLocation(), "Location should be updated.");
        // Using compareTo for BigDecimal comparison
        assertTrue(new BigDecimal("92.5").compareTo(fetchedScooter.getBatteryLevel()) == 0, "Battery level should be updated.");
        assertEquals(initialScooter.getScooterCode(), fetchedScooter.getScooterCode(), "Scooter code should remain unchanged.");
        assertEquals(initialScooter.getAvailable(), fetchedScooter.getAvailable(), "Availability should remain unchanged for this test.");
    }

    @Test
    void testAdminUpdatesScooterStatus() {
        // 1. Create and save a scooter, initially available
        Scooter scooter = new Scooter();
        scooter.setScooterCode("ADMIN_S002_STATUS");
        scooter.setModel("Beta Model");
        scooter.setBatteryLevel(new BigDecimal("88.0"));
        scooter.setLocation("West Depot");
        scooter.setAvailable(true);
        scooter.setLastMaintenanceDate(new Date());

        Scooter savedScooter = scooterService.addScooter(scooter);
        assertNotNull(savedScooter, "Saved scooter should not be null.");
        Long scooterId = savedScooter.getId();
        assertTrue(savedScooter.getAvailable(), "Scooter should initially be available.");

        // 2. Update status to unavailable (e.g., MAINTENANCE)
        // Assuming ScooterService.updateScooterStatus handles the internal logic for status strings if any
        // For this test, we directly update the boolean 'isAvailable' via a dedicated method if it exists
        // or via the general updateScooter method.
        // Let's assume there's a direct method updateScooterStatus(Long id, boolean isAvailable)
        // If not, this part needs to be adjusted based on ScooterService API.
        // For now, we'll simulate it by fetching, setting, and using updateScooter if no direct status update method.
        // However, ScooterServiceImpl has updateScooterStatus(Long scooterId, String status) which maps to boolean.

        scooterService.updateScooterStatus(scooterId, "Unavailable"); // Or specific status string like "MAINTENANCE"
        Scooter statusUpdatedScooter = scooterService.getScooterById(scooterId);
        assertNotNull(statusUpdatedScooter, "Scooter should exist after status update.");
        assertFalse(statusUpdatedScooter.getAvailable(), "Scooter should be unavailable after status update.");

        // 3. Update status back to available
        scooterService.updateScooterStatus(scooterId, "Available");
        Scooter statusRevertedScooter = scooterService.getScooterById(scooterId);
        assertNotNull(statusRevertedScooter, "Scooter should exist after status revert.");
        assertTrue(statusRevertedScooter.getAvailable(), "Scooter should be available again.");
    }

    @Test
    void testAdminDeletesScooter_Success() {
        // 1. Create and save a scooter
        Scooter scooterToDelete = new Scooter();
        scooterToDelete.setScooterCode("ADMIN_S003_DELETE");
        scooterToDelete.setModel("Gamma Model");
        scooterToDelete.setBatteryLevel(new BigDecimal("75.0"));
        scooterToDelete.setLocation("South Depot");
        scooterToDelete.setAvailable(true);
        scooterToDelete.setLastMaintenanceDate(new Date());

        Scooter savedScooter = scooterService.addScooter(scooterToDelete);
        assertNotNull(savedScooter, "Saved scooter for deletion should not be null.");
        Long scooterId = savedScooter.getId();

        // 2. Delete the scooter
        scooterService.deleteScooter(scooterId);

        // 3. Verify it's deleted
        // Option A: ScooterService.getScooterById might throw an exception or return null
        // Let's assume it throws EntityNotFoundException or similar, or returns null which we check
        Scooter foundScooter = scooterService.getScooterById(scooterId); // Assuming this returns null or is handled
        assertNull(foundScooter, "Scooter should be null after deletion if getScooterById returns null for non-existent.");

        // Option B: More direct repository check (often preferred in integration tests for verification)
        assertFalse(scooterRepository.findById(scooterId).isPresent(), "Scooter should not be found in repository after deletion.");
    }
} 