package org.example.escooter_booking_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_config") // 表名可以自定义
public class AppConfig {

    @Id
    @Column(name = "config_key", length = 100) // 配置键，例如 "price_per_minute"
    private String key;

    @Column(name = "config_value", length = 255) // 配置值，例如 "0.15"
    private String value;

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}