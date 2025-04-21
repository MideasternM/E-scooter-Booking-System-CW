package org.example.escooter_booking_system.controller;

import org.example.escooter_booking_system.service.AppConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/pricing") // Public API path for pricing queries
public class PricingController {

    private static final Logger log = LoggerFactory.getLogger(PricingController.class);

    @Autowired
    private AppConfigService appConfigService;

    // Get the price per minute for a specific scooter model
    @GetMapping("/models/{modelName}")
    public ResponseEntity<Map<String, BigDecimal>> getPriceForModel(@PathVariable String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            log.warn("Bad request for getPriceForModel: modelName is empty");
            return ResponseEntity.badRequest().build();
        }
        try {
            BigDecimal price = appConfigService.getPriceForModel(modelName);
            // Return in a structure like { "pricePerMinute": 0.20 }
            return ResponseEntity.ok(Collections.singletonMap("pricePerMinute", price));
        } catch (Exception e) {
            log.error("Error fetching price for model {}", modelName, e);
            // Consider returning a default price or a more specific error
            // Returning default price for now to avoid breaking frontend expecting a value
            BigDecimal defaultPrice = appConfigService.getPriceForModel(null); // Gets the default
            return ResponseEntity.ok(Collections.singletonMap("pricePerMinute", defaultPrice));
            // Or: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}