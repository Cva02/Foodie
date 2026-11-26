package com.foodie.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RiderRegisterRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[+0-9 ()-]{7,20}$",
                message = "Enter a valid phone number")
        String phone,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72)
        String password,

        @NotBlank(message = "Vehicle type is required")
        @Pattern(regexp = "^(Bike|Scooty)$",
                message = "Vehicle must be Bike or Scooty")
        String vehicle,

        @NotBlank(message = "Vehicle number is required")
        @Pattern(regexp = "^[A-Za-z0-9 -]{4,20}$")
        String vehicleNumber,

        @NotBlank(message = "Driving licence number is required")
        @Pattern(regexp = "^[A-Za-z0-9/-]{5,30}$")
        String licenseNumber,

        @NotBlank(message = "City is required")
        @Size(max = 100)
        String city,

        @NotBlank(message = "Service PIN codes are required")
        @Pattern(
                regexp = "^\\s*[0-9]{6}\\s*,\\s*[0-9]{6}\\s*$",
                message = "Enter two six-digit delivery PIN codes separated by a comma"
        )
        String servicePinCodes,

        @NotBlank(message = "Bank name is required")
        @Size(max = 100)
        String bankName,

        @NotBlank(message = "Account holder name is required")
        @Size(max = 100)
        String accountHolderName,

        @NotBlank(message = "Bank account number is required")
        @Pattern(regexp = "^[0-9]{6,20}$")
        String bankAccountNumber,

        @NotBlank(message = "IFSC code is required")
        @Pattern(
                regexp = "^[A-Za-z]{4}0[A-Za-z0-9]{6}$",
                message = "Enter a valid IFSC code"
        )
        String bankIfsc
) {
}