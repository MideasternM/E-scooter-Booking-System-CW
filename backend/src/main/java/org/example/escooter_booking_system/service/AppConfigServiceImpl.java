package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.AppConfig;
import org.example.escooter_booking_system.repository.AppConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppConfigServiceImpl implements AppConfigService {

    private static final Logger log = LoggerFactory.getLogger(AppConfigServiceImpl.class);
    // public static final String PRICE_PER_MINUTE_KEY = "price_per_minute"; // Can
    // keep if you also want a global default
    private static final String MODEL_PRICE_PREFIX = "price_model_"; // Prefix for model price keys
    private static final BigDecimal DEFAULT_MODEL_PRICE = new BigDecimal("0.15"); // Default price if model specific not
                                                                                  // found

    @Autowired
    private AppConfigRepository appConfigRepository;

    @Override
    public String getConfigValue(String key) {
        return appConfigRepository.findByKey(key)
                .map(AppConfig::getValue)
                .orElse(null); // Or return a default
    }

    // Added overload implementation
    @Override
    public String getConfigValue(String key, String defaultValue) {
        return appConfigRepository.findByKey(key)
                .map(AppConfig::getValue)
                .orElse(defaultValue);
    }

    // Helper to create the key for storing model price
    private String getConfigKeyForModel(String model) {
        // Ensure key validity, e.g., replace spaces or special chars
        String sanitizedModel = model.replaceAll("[^a-zA-Z0-9_-]", "_");
        return MODEL_PRICE_PREFIX + sanitizedModel;
    }

    // Added implementation
    @Override
    public BigDecimal getPriceForModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            log.warn("Attempted to get price for null or empty model, returning default price.");
            return DEFAULT_MODEL_PRICE;
        }
        String key = getConfigKeyForModel(model);
        String value = getConfigValue(key, null); // Don't use default here, check DB first

        if (value == null) {
            log.warn("Config key '{}' for model '{}' not found, using default model price: {}", key, model,
                    DEFAULT_MODEL_PRICE);
            return DEFAULT_MODEL_PRICE;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.error("Invalid format for config key '{}': value '{}'. Using default model price: {}", key, value,
                    DEFAULT_MODEL_PRICE, e);
            return DEFAULT_MODEL_PRICE;
        }
    }

    // Added implementation
    @Override
    public void updatePriceForModel(String model, BigDecimal price) {
        if (model == null || model.trim().isEmpty() || price == null) {
            log.error("Cannot update price for null/empty model or null price.");
            return; // Or throw exception
        }
        String key = getConfigKeyForModel(model);
        updateConfigValue(key, price.toPlainString()); // Use toPlainString to avoid scientific notation
    }

    // Added implementation
    @Override
    public Map<String, BigDecimal> getAllModelPrices(List<String> models) {
        Map<String, BigDecimal> prices = new HashMap<>();
        if (models != null) {
            for (String model : models) {
                if (model != null && !model.trim().isEmpty()) { // Ensure model name is valid
                    prices.put(model, getPriceForModel(model));
                }
            }
        }
        return prices;
    }

    // Keep existing implementation
    @Override
    public void updateConfigValue(String key, String value) {
        AppConfig config = appConfigRepository.findByKey(key)
                .orElse(new AppConfig()); // Create new if not exists
        config.setKey(key);
        config.setValue(value);
        appConfigRepository.save(config);
        log.info("Updated config key '{}' to value '{}'", key, value);
    }
}