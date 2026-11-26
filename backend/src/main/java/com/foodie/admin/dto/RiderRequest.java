package com.foodie.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body of POST /api/riders and PUT /api/riders/{id}. */
public record RiderRequest(
        @NotBlank(message = "Rider name is required") String name,
        String avatar,
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[+0-9 ()-]{7,20}$", message = "Phone number looks invalid") String phone,
        @NotBlank(message = "Email is required") @Email(message = "Email looks invalid") String email,
        @NotBlank(message = "Vehicle type is required") String vehicle,
        @NotBlank(message = "Vehicle number is required") String vehicleNumber,
        @NotBlank(message = "Driving licence number is required") String licenseNumber,
        @NotBlank(message = "City / service area is required") String city,
        String status,          // default OFFLINE
        @Size(max = 1000, message = "The reason must be 1000 characters or fewer") String statusNote) {
}
