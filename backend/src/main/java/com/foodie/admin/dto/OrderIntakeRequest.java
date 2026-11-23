package com.foodie.admin.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record OrderIntakeRequest(
        @NotBlank @Size(max = 20) String orderId,
        @NotBlank @Size(max = 100) String customerName,
        @NotBlank @Pattern(regexp = "^[+0-9 ()-]{7,20}$") String customerPhone,
        @NotBlank @Size(max = 500) String customerAddress,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "Customer PIN code must be six digits")
        String customerPinCode,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double customerLatitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double customerLongitude,
        @NotBlank @Size(max = 20) String sellerId,
        @NotBlank @Size(max = 100) String sellerName,
        @NotBlank @Size(max = 500) String sellerAddress,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double sellerLatitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double sellerLongitude,
        @NotBlank @Size(max = 150) String itemName,
        @Min(1) int quantity,
        @DecimalMin("0.01") double amount,
        @NotBlank @Size(max = 40) String payment,
        Instant expectedDeliveryAt) {
}
