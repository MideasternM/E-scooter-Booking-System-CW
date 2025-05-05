package org.example.escooter_booking_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Data
@Table(name = "rental_stores")
public class RentalStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 门店名称

    @Column(unique = true)
    private String code; // 门店编号

    @Column(nullable = false)
    private Double latitude; // 纬度

    @Column(nullable = false)
    private Double longitude; // 经度

    @Column(nullable = false)
    private String address; // 详细地址

    // Assuming Scooter IDs are stored directly for simplicity
    // For a more relational approach, this would be a relationship to Scooter
    // entities
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rental_store_available_scooters", joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "scooter_code") // Store scooter identifiers (e.g., codes or serial numbers)
    private List<String> availableScooters = new ArrayList<>(); // 可供租赁的 Scooter ID 列表

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreStatus status = StoreStatus.OPERATIONAL; // 门店状态

    @Column(nullable = false)
    private String openingHours = "09:00-18:00"; // 营业时间

    private String contactPhone; // 联系电话

    @Lob // Use Lob for potentially long descriptions
    private String description; // 门店描述

    private String imageUrl; // 门店图片

    @ElementCollection(targetClass = StoreService.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "rental_store_services", joinColumns = @JoinColumn(name = "store_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service", nullable = false)
    private List<StoreService> servicesOffered = new ArrayList<>(List.of(StoreService.RENTAL)); // 提供的服务

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime lastUpdateTime = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        lastUpdateTime = LocalDateTime.now();
    }

    public enum StoreStatus {
        OPERATIONAL, // 正常营业
        CLOSED_TEMPORARY, // 临时关闭
        CLOSED_PERMANENT // 永久关闭
    }

    public enum StoreService {
        RENTAL, // 提供租赁
        REPAIR, // 提供维修
        SALES, // 提供销售
        CHARGING // 提供充电
    }
}