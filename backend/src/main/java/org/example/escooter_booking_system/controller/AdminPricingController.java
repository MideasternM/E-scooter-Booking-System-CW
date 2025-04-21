package org.example.escooter_booking_system.controller;

import org.example.escooter_booking_system.repository.ScooterRepository;
import org.example.escooter_booking_system.service.AppConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/pricing") // Base path for admin pricing config
public class AdminPricingController {

    private static final Logger log = LoggerFactory.getLogger(AdminPricingController.class);

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private ScooterRepository scooterRepository; // Inject ScooterRepository to get models

    // Get current prices for all distinct models found in the scooter table
    @GetMapping("/models")
    public ResponseEntity<Map<String, BigDecimal>> getAllModelPrices() {
        try {
            List<String> models = scooterRepository.findDistinctModels(); // Get all distinct models
            Map<String, BigDecimal> prices = appConfigService.getAllModelPrices(models);
            return ResponseEntity.ok(prices);
        } catch (Exception e) {
            log.error("Error fetching all model prices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Update the price for a specific model
    @PutMapping("/models/{modelName}")
    public ResponseEntity<Void> updateModelPrice(
            @PathVariable String modelName,
            @RequestBody Map<String, String> payload) {
        String newPriceStr = payload.get("pricePerMinute"); // Expecting a JSON like { "pricePerMinute": "0.20" }
        if (newPriceStr == null || modelName == null) {
            log.warn("Bad request for updateModelPrice: modelName={} or payload invalid", modelName);
            return ResponseEntity.badRequest().build();
        }
        try {
            BigDecimal newPrice = new BigDecimal(newPriceStr);
            // Optional: Add validation if needed (e.g., price > 0)
            appConfigService.updatePriceForModel(modelName, newPrice);
            return ResponseEntity.ok().build();
        } catch (NumberFormatException e) {
            log.error("Invalid price format received for model '{}': {}", modelName, newPriceStr, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating price for model {}", modelName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}