package com.foodie.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RiderRegisterRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "^[+0-9 ()-]{7,20}$") String phone,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Pattern(regexp = "^(Bike|Scooty)$") String vehicle,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9 -]{4,20}$") String vehicleNumber,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9/-]{5,30}$") String licenseNumber,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Pattern(regexp = "^\\s*[0-9]{6}\\s*,\\s*[0-9]{6}\\s*$",
                message = "Enter two six-digit delivery PIN codes separated by a comma") String servicePinCodes,
        @NotBlank @Size(max = 100) String bankName,
        @NotBlank @Size(max = 100) String accountHolderName,
        @NotBlank @Pattern(regexp = "^[0-9]{6,20}$") String bankAccountNumber,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{4}0[A-Za-z0-9]{6}$") String bankIfsc) {
}
