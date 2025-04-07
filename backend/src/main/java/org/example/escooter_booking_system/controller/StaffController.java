package org.example.escooter_booking_system.controller;

import org.example.escooter_booking_system.model.Staff;
import org.example.escooter_booking_system.model.StaffLoginRequest;
import org.example.escooter_booking_system.model.StaffRegistrationRequest;
import org.example.escooter_booking_system.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @PostMapping
    public ResponseEntity<Staff> addStaff(@RequestBody Staff staff) {
        Staff newStaff = staffService.addStaff(staff);
        return ResponseEntity.ok(newStaff);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Staff> getStaffById(@PathVariable Long id) {
        Staff staff = staffService.getStaffById(id);
        if (staff != null) {
            return ResponseEntity.ok(staff);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Staff>> getAllStaff() {
        List<Staff> staffList = staffService.getAllStaff();
        return ResponseEntity.ok(staffList);
    }

    @GetMapping("/number/{staffNumber}")
    public ResponseEntity<Staff> getStaffByNumber(@PathVariable String staffNumber) {
        Staff staff = staffService.getStaffByNumber(staffNumber);
        if (staff != null) {
            return ResponseEntity.ok(staff);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Staff> updateStaff(
            @PathVariable Long id,
            @RequestBody Staff staffDetails) {
        Staff updatedStaff = staffService.updateStaff(id, staffDetails);
        if (updatedStaff != null) {
            return ResponseEntity.ok(updatedStaff);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Staff>> getStaffByStatus(@PathVariable String status) {
        List<Staff> staffList = staffService.getStaffByStatus(status);
        return ResponseEntity.ok(staffList);
    }

    @GetMapping("/area/{area}")
    public ResponseEntity<List<Staff>> getStaffByArea(@PathVariable String area) {
        List<Staff> staffList = staffService.getStaffByArea(area);
        return ResponseEntity.ok(staffList);
    }

    @GetMapping("/position/{position}")
    public ResponseEntity<List<Staff>> getStaffByPosition(@PathVariable String position) {
        List<Staff> staffList = staffService.getStaffByPosition(position);
        return ResponseEntity.ok(staffList);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Staff> updateStaffStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Staff updatedStaff = staffService.updateStaffStatus(id, status);
        if (updatedStaff != null) {
            return ResponseEntity.ok(updatedStaff);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/area")
    public ResponseEntity<Staff> updateStaffArea(
            @PathVariable Long id,
            @RequestParam String area) {
        Staff updatedStaff = staffService.updateStaffArea(id, area);
        if (updatedStaff != null) {
            return ResponseEntity.ok(updatedStaff);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginStaff(@RequestBody StaffLoginRequest loginRequest) {
        Map<String, Object> result = staffService.authenticateStaff(
                loginRequest.getStaffNumber(),
                loginRequest.getPassword());

        if (result != null) {
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.status(401).body(Map.of("message", "Invalid staff number or password"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerStaff(@RequestBody StaffRegistrationRequest registrationRequest) {
        // Check if staff with the same number already exists
        Staff existingStaff = staffService.getStaffByNumber(registrationRequest.getStaffNumber());
        if (existingStaff != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Staff with this number already exists"));
        }

        // Create new staff object from registration request
        Staff newStaff = new Staff();
        newStaff.setStaffNumber(registrationRequest.getStaffNumber());
        newStaff.setName(registrationRequest.getName());
        newStaff.setPosition(registrationRequest.getPosition());
        newStaff.setPhoneNumber(registrationRequest.getPhoneNumber());
        newStaff.setEmail(registrationRequest.getEmail());
        newStaff.setPassword(registrationRequest.getPassword());
        newStaff.setArea(registrationRequest.getArea());

        // Set default status if not provided
        String status = registrationRequest.getStatus();
        newStaff.setStatus(status != null ? status : "Active");

        // Save the new staff
        Staff registeredStaff = staffService.addStaff(newStaff);

        return ResponseEntity.ok(registeredStaff);
    }
}