package org.example.escooter_booking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapItemDTO {
    private Long id;
    private String type; // "scooter" 或 "store"
    private Double latitude;
    private Double longitude;

    // Scooter相关字段
    private Boolean isAvailable;
    private BigDecimal batteryLevel;
    private String model;

    // Store相关字段
    private String name;
    private String code;
    private String status;
    private Integer availableScooterCount;
    private String address;
}