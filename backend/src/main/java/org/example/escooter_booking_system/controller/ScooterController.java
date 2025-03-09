package org.example.escooter_booking_system.controller;

import org.example.escooter_booking_system.model.Scooter;
import org.example.escooter_booking_system.service.ScooterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scooters")
public class ScooterController {

    @Autowired
    private ScooterService scooterService;

    @PostMapping
    public ResponseEntity<Scooter> addScooter(@RequestBody Scooter scooter) {
        Scooter addedScooter = scooterService.addScooter(scooter);
        return ResponseEntity.ok(addedScooter);
    }

    @GetMapping
    public ResponseEntity<List<Scooter>> getAllScooters() {
        List<Scooter> scooters = scooterService.getAllScooters();
        return ResponseEntity.ok(scooters);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Scooter>> getAvailableScooters() {
        List<Scooter> scooters = scooterService.getAvailableScooters();
        return ResponseEntity.ok(scooters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Scooter> getScooterById(@PathVariable Long id) {
        Scooter scooter = scooterService.getScooterById(id);
        if (scooter != null) {
            return ResponseEntity.ok(scooter);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Scooter> updateScooter(@PathVariable Long id,
            @RequestBody Scooter scooterDetails) {
        Scooter updatedScooter = scooterService.updateScooter(id, scooterDetails);
        if (updatedScooter != null) {
            return ResponseEntity.ok(updatedScooter);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScooter(@PathVariable Long id) {
        scooterService.deleteScooter(id);
        return ResponseEntity.noContent().build();
    }
}
