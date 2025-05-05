package org.example.escooter_booking_system.controller;

import org.example.escooter_booking_system.dto.MapItemDTO;
import org.example.escooter_booking_system.model.RentalStore;
import org.example.escooter_booking_system.model.Scooter;
import org.example.escooter_booking_system.service.RentalStoreService;
import org.example.escooter_booking_system.service.ScooterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/map-items")
public class MapController {

    @Autowired
    private ScooterService scooterService;

    @Autowired
    private RentalStoreService rentalStoreService;

    @GetMapping
    public ResponseEntity<List<MapItemDTO>> getAllMapItems() {
        List<MapItemDTO> mapItems = new ArrayList<>();

        // 获取所有滑板车并转换为地图项目
        List<Scooter> scooters = scooterService.getAllScooters();
        List<MapItemDTO> scooterItems = scooters.stream()
                .map(scooter -> MapItemDTO.builder()
                        .id(scooter.getId())
                        .type("scooter")
                        .latitude(scooter.getLatitude())
                        .longitude(scooter.getLongitude())
                        .batteryLevel(scooter.getBatteryLevel())
                        .model(scooter.getModel())
                        .isAvailable(scooter.getAvailable())
                        .build())
                .collect(Collectors.toList());
        mapItems.addAll(scooterItems);

        // 获取所有门店并转换为地图项目
        List<RentalStore> stores = rentalStoreService.getAllRentalStores();
        List<MapItemDTO> storeItems = stores.stream()
                .map(store -> MapItemDTO.builder()
                        .id(store.getId())
                        .type("store")
                        .latitude(store.getLatitude())
                        .longitude(store.getLongitude())
                        .name(store.getName())
                        .code(store.getCode())
                        .status(store.getStatus().toString())
                        .availableScooterCount(store.getAvailableScooters().size())
                        .address(store.getAddress())
                        .build())
                .collect(Collectors.toList());
        mapItems.addAll(storeItems);

        return ResponseEntity.ok(mapItems);
    }
}