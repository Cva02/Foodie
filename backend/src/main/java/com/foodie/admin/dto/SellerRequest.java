package com.foodie.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body of POST /api/sellers and PUT /api/sellers/{id}. */
public record SellerRequest(
        @NotBlank(message = "Restaurant name is required") String name,
        @NotBlank(message = "Owner name is required") String ownerName,
        String ownerAvatar,
        String image,
        @NotBlank(message = "Cuisine is required") String cuisine,
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[+0-9 ()-]{7,20}$", message = "Phone number looks invalid") String phone,
        @NotBlank(message = "Email is required") @Email(message = "Email looks invalid") String email,
        @NotBlank(message = "Address is required") String address,
        @NotBlank(message = "City is required") String city,
        @NotBlank(message = "FSSAI number is required") String fssaiNumber,
        String status,          // ACTIVE | PENDING | SUSPENDED  (default ACTIVE)
        Boolean open,
        @Size(max = 1000, message = "The reason must be 1000 characters or fewer") String statusNote) {
}
