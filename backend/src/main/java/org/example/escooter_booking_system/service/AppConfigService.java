package org.example.escooter_booking_system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AppConfigService {
    String getConfigValue(String key);

    String getConfigValue(String key, String defaultValue);

    void updateConfigValue(String key, String value);

    BigDecimal getPriceForModel(String model);

    void updatePriceForModel(String model, BigDecimal price);

    Map<String, BigDecimal> getAllModelPrices(List<String> models);
}