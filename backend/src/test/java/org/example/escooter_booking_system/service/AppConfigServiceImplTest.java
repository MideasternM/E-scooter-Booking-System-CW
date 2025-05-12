package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.AppConfig;
import org.example.escooter_booking_system.repository.AppConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// import org.slf4j.Logger; // Assuming no direct logger testing for now

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppConfigServiceImplTest {

    @Mock
    private AppConfigRepository appConfigRepository;

    @InjectMocks
    private AppConfigServiceImpl appConfigService;

    private final BigDecimal DEFAULT_PRICE = new BigDecimal("0.15");
    private final String MODEL_X = "Model X";
    private final String KEY_MODEL_X = "price_model_Model_X"; // Sanitized key for "Model X"

    @BeforeEach
    void setUp() {
        // No specific setup needed for each test as mocks are re-initialized
    }

    private String getSanitizedModelKey(String modelName) {
        String sanitizedModel = modelName.replaceAll("[^a-zA-Z0-9_-]", "_");
        return "price_model_" + sanitizedModel;
    }

    // --- Tests for getPriceForModel ---
    @Test
    void getPriceForModel_whenConfiguredAndValid_shouldReturnPrice() {
        // Arrange
        AppConfig config = new AppConfig();
        config.setKey(KEY_MODEL_X);
        config.setValue("0.25");
        when(appConfigRepository.findByKey(KEY_MODEL_X)).thenReturn(Optional.of(config));

        // Act
        BigDecimal price = appConfigService.getPriceForModel(MODEL_X);

        // Assert
        assertEquals(new BigDecimal("0.25"), price);
        verify(appConfigRepository, times(1)).findByKey(KEY_MODEL_X);
    }

    @Test
    void getPriceForModel_whenNotConfigured_shouldReturnDefaultPrice() {
        // Arrange
        when(appConfigRepository.findByKey(KEY_MODEL_X)).thenReturn(Optional.empty());

        // Act
        BigDecimal price = appConfigService.getPriceForModel(MODEL_X);

        // Assert
        assertEquals(DEFAULT_PRICE, price);
        verify(appConfigRepository, times(1)).findByKey(KEY_MODEL_X);
    }

    @Test
    void getPriceForModel_whenConfiguredButInvalidFormat_shouldReturnDefaultPrice() {
        // Arrange
        AppConfig config = new AppConfig();
        config.setKey(KEY_MODEL_X);
        config.setValue("invalid_price");
        when(appConfigRepository.findByKey(KEY_MODEL_X)).thenReturn(Optional.of(config));

        // Act
        BigDecimal price = appConfigService.getPriceForModel(MODEL_X);

        // Assert
        assertEquals(DEFAULT_PRICE, price);
        verify(appConfigRepository, times(1)).findByKey(KEY_MODEL_X);
    }

    @Test
    void getPriceForModel_whenModelIsNull_shouldReturnDefaultPrice() {
        // Act
        BigDecimal price = appConfigService.getPriceForModel(null);
        // Assert
        assertEquals(DEFAULT_PRICE, price);
        verify(appConfigRepository, never()).findByKey(anyString());
    }

    @Test
    void getPriceForModel_whenModelIsEmpty_shouldReturnDefaultPrice() {
        // Act
        BigDecimal price = appConfigService.getPriceForModel("");
        // Assert
        assertEquals(DEFAULT_PRICE, price);
        verify(appConfigRepository, never()).findByKey(anyString());
    }

    @Test
    void getPriceForModel_whenModelHasSpecialChars_usesSanitizedKey() {
        // Arrange
        String modelWithSpecialChars = "Model S /@#";
        String sanitizedKey = getSanitizedModelKey(modelWithSpecialChars); // "price_model_Model_S___"
        AppConfig config = new AppConfig();
        config.setKey(sanitizedKey);
        config.setValue("0.30");
        when(appConfigRepository.findByKey(sanitizedKey)).thenReturn(Optional.of(config));

        // Act
        BigDecimal price = appConfigService.getPriceForModel(modelWithSpecialChars);

        // Assert
        assertEquals(new BigDecimal("0.30"), price);
        verify(appConfigRepository, times(1)).findByKey(sanitizedKey);
    }

    // --- Tests for updatePriceForModel ---
    @Test
    void updatePriceForModel_whenNewModel_shouldCreateConfig() {
        // Arrange
        BigDecimal newPrice = new BigDecimal("0.22");
        when(appConfigRepository.findByKey(KEY_MODEL_X)).thenReturn(Optional.empty()); // Simulate not found
        // ArgumentCaptor for AppConfig
        ArgumentCaptor<AppConfig> appConfigCaptor = ArgumentCaptor.forClass(AppConfig.class);
        when(appConfigRepository.save(appConfigCaptor.capture())).thenReturn(null); // Actual return doesn't matter for void method logic

        // Act
        appConfigService.updatePriceForModel(MODEL_X, newPrice);

        // Assert
        verify(appConfigRepository, times(1)).findByKey(KEY_MODEL_X);
        verify(appConfigRepository, times(1)).save(any(AppConfig.class));
        AppConfig savedConfig = appConfigCaptor.getValue();
        assertEquals(KEY_MODEL_X, savedConfig.getKey());
        assertEquals("0.22", savedConfig.getValue());
    }

    @Test
    void updatePriceForModel_whenExistingModel_shouldUpdateConfig() {
        // Arrange
        BigDecimal newPrice = new BigDecimal("0.28");
        AppConfig existingConfig = new AppConfig();
        existingConfig.setKey(KEY_MODEL_X);
        existingConfig.setValue("0.20");
        when(appConfigRepository.findByKey(KEY_MODEL_X)).thenReturn(Optional.of(existingConfig));
        ArgumentCaptor<AppConfig> appConfigCaptor = ArgumentCaptor.forClass(AppConfig.class);
        when(appConfigRepository.save(appConfigCaptor.capture())).thenReturn(null);

        // Act
        appConfigService.updatePriceForModel(MODEL_X, newPrice);

        // Assert
        verify(appConfigRepository, times(1)).findByKey(KEY_MODEL_X);
        verify(appConfigRepository, times(1)).save(any(AppConfig.class));
        AppConfig savedConfig = appConfigCaptor.getValue();
        assertEquals(KEY_MODEL_X, savedConfig.getKey());
        assertEquals("0.28", savedConfig.getValue()); // Ensure the value is updated
        assertSame(existingConfig, savedConfig, "Should update the existing config instance");
    }

    @Test
    void updatePriceForModel_whenModelIsNull_shouldNotSave() {
        // Act
        appConfigService.updatePriceForModel(null, new BigDecimal("0.20"));
        // Assert
        verify(appConfigRepository, never()).save(any(AppConfig.class));
    }

    @Test
    void updatePriceForModel_whenPriceIsNull_shouldNotSave() {
        // Act
        appConfigService.updatePriceForModel(MODEL_X, null);
        // Assert
        verify(appConfigRepository, never()).save(any(AppConfig.class));
    }

     @Test
    void updatePriceForModel_modelNameWithSpecialChars_usesSanitizedKeyForSave() {
        String modelWithSpecialChars = "Model Y Pro+";
        String sanitizedKey = getSanitizedModelKey(modelWithSpecialChars);
        BigDecimal newPrice = new BigDecimal("0.35");

        when(appConfigRepository.findByKey(sanitizedKey)).thenReturn(Optional.empty());
        ArgumentCaptor<AppConfig> appConfigCaptor = ArgumentCaptor.forClass(AppConfig.class);
        when(appConfigRepository.save(appConfigCaptor.capture())).thenReturn(null);

        appConfigService.updatePriceForModel(modelWithSpecialChars, newPrice);

        verify(appConfigRepository).save(any(AppConfig.class));
        assertEquals(sanitizedKey, appConfigCaptor.getValue().getKey());
        assertEquals("0.35", appConfigCaptor.getValue().getValue());
    }

    // --- Tests for getAllModelPrices ---
    @Test
    void getAllModelPrices_shouldReturnMapWithCorrectPricesOrDefault() {
        // Arrange
        String modelA = "ModelA"; // Configured, valid
        String keyModelA = getSanitizedModelKey(modelA);
        AppConfig configA = new AppConfig(); configA.setKey(keyModelA); configA.setValue("0.20");

        String modelB = "ModelB"; // Not configured
        String keyModelB = getSanitizedModelKey(modelB);

        String modelC = "ModelC"; // Configured, invalid format
        String keyModelC = getSanitizedModelKey(modelC);
        AppConfig configC = new AppConfig(); configC.setKey(keyModelC); configC.setValue("invalid");

        String modelD_Special = "Model D!"; // Configured, special chars
        String keyModelD_Special = getSanitizedModelKey(modelD_Special);
        AppConfig configD = new AppConfig(); configD.setKey(keyModelD_Special); configD.setValue("0.40");

        List<String> models = Arrays.asList(modelA, modelB, modelC, modelD_Special, null, "");

        when(appConfigRepository.findByKey(keyModelA)).thenReturn(Optional.of(configA));
        when(appConfigRepository.findByKey(keyModelB)).thenReturn(Optional.empty());
        when(appConfigRepository.findByKey(keyModelC)).thenReturn(Optional.of(configC));
        when(appConfigRepository.findByKey(keyModelD_Special)).thenReturn(Optional.of(configD));

        // Act
        Map<String, BigDecimal> prices = appConfigService.getAllModelPrices(models);

        // Assert
        assertNotNull(prices);
        assertEquals(4, prices.size(), "Should contain prices for valid models provided"); // modelA, modelB, modelC, modelD_Special
        assertEquals(new BigDecimal("0.20"), prices.get(modelA));
        assertEquals(DEFAULT_PRICE, prices.get(modelB)); // Not configured, should use default
        assertEquals(DEFAULT_PRICE, prices.get(modelC)); // Invalid format, should use default
        assertEquals(new BigDecimal("0.40"), prices.get(modelD_Special));
        assertFalse(prices.containsKey(null));
        assertFalse(prices.containsKey(""));
    }

    @Test
    void getAllModelPrices_whenModelListIsNull_shouldReturnEmptyMap() {
        // Act
        Map<String, BigDecimal> prices = appConfigService.getAllModelPrices(null);
        // Assert
        assertNotNull(prices);
        assertTrue(prices.isEmpty());
    }

    @Test
    void getAllModelPrices_whenModelListIsEmpty_shouldReturnEmptyMap() {
        // Act
        Map<String, BigDecimal> prices = appConfigService.getAllModelPrices(Collections.emptyList());
        // Assert
        assertNotNull(prices);
        assertTrue(prices.isEmpty());
    }

    // --- Tests for updateConfigValue ---
    @Test
    void updateConfigValue_whenKeyExists_shouldUpdateExistingConfig() {
        // Arrange
        String key = "test_key";
        String oldValue = "old_value";
        String newValue = "new_value";
        AppConfig existingConfig = new AppConfig();
        existingConfig.setKey(key);
        existingConfig.setValue(oldValue);

        when(appConfigRepository.findByKey(key)).thenReturn(Optional.of(existingConfig));
        ArgumentCaptor<AppConfig> configCaptor = ArgumentCaptor.forClass(AppConfig.class);
        when(appConfigRepository.save(configCaptor.capture())).thenReturn(null); // Return value of save doesn't matter here

        // Act
        appConfigService.updateConfigValue(key, newValue);

        // Assert
        verify(appConfigRepository, times(1)).findByKey(key);
        verify(appConfigRepository, times(1)).save(any(AppConfig.class));
        AppConfig savedConfig = configCaptor.getValue();
        assertSame(existingConfig, savedConfig, "Should be the same instance being updated");
        assertEquals(key, savedConfig.getKey());
        assertEquals(newValue, savedConfig.getValue());
    }

    @Test
    void updateConfigValue_whenKeyDoesNotExist_shouldCreateNewConfig() {
        // Arrange
        String key = "new_test_key";
        String value = "new_value";

        when(appConfigRepository.findByKey(key)).thenReturn(Optional.empty());
        ArgumentCaptor<AppConfig> configCaptor = ArgumentCaptor.forClass(AppConfig.class);
        when(appConfigRepository.save(configCaptor.capture())).thenReturn(null); // Return value of save doesn't matter here

        // Act
        appConfigService.updateConfigValue(key, value);

        // Assert
        verify(appConfigRepository, times(1)).findByKey(key);
        verify(appConfigRepository, times(1)).save(any(AppConfig.class));
        AppConfig savedConfig = configCaptor.getValue();
        assertNotNull(savedConfig);
        assertEquals(key, savedConfig.getKey());
        assertEquals(value, savedConfig.getValue());
    }
} 