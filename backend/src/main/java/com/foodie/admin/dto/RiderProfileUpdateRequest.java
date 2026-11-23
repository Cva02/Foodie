package com.foodie.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RiderProfileUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Pattern(regexp = "^[+0-9 ()-]{7,20}$") String phone,
        @NotBlank @Pattern(regexp = "^(Bike|Scooty)$") String vehicle,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9 -]{4,20}$") String vehicleNumber,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9/-]{5,30}$") String licenseNumber,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Pattern(regexp = "^\\s*[0-9]{6}\\s*,\\s*[0-9]{6}\\s*$",
                message = "Enter two six-digit delivery PIN codes separated by a comma") String servicePinCodes) {
}
