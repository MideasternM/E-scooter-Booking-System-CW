package org.example.escooter_booking_system.dto;

import lombok.Data;
import org.example.escooter_booking_system.model.RentalStore;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class RentalStoreDTO {

    @NotBlank(message = "Store name cannot be blank")
    @Size(max = 100, message = "Store name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Store code cannot be blank")
    @Size(max = 50, message = "Store code cannot exceed 50 characters")
    // Add regex pattern if code has specific format: @Pattern(regexp =
    // "^[A-Z0-9]+$", message = "Invalid code format")
    private String code;

    @NotNull(message = "Latitude cannot be null")
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude cannot be null")
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    private Double longitude;

    @NotBlank(message = "Address cannot be blank")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    // Add status field to allow status updates
    private RentalStore.StoreStatus status;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]-([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Opening hours must be in HH:mm-HH:mm format")
    private String openingHours = "09:00-18:00";

    @Size(max = 20, message = "Contact phone cannot exceed 20 characters")
    private String contactPhone;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    // Consider URL validation: @URL(message = "Invalid image URL format")
    private String imageUrl;

    // Services offered might be handled separately or pre-defined
    private List<RentalStore.StoreService> servicesOffered;

    // Available scooters are usually managed via separate actions, not directly in
    // the store DTO
    // private List<String> availableScooters;
}