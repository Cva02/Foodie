
package com.foodie.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SellerRegistrationRequest(

        @NotBlank(message = "Restaurant name is required")
        @Size(max = 120)
        String restaurantName,

        @NotBlank(message = "Owner name is required")
        @Size(max = 120)
        String ownerName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email looks invalid")
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[6-9]\\d{9}$",
                 message = "Enter a valid 10-digit mobile number")
        String phone,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 72)
        String password,

        @NotBlank(message = "Cuisine is required")
        @Size(max = 120)
        String cuisine,

        @NotBlank(message = "Address is required")
        @Size(max = 1000)
        String address,

        @NotBlank(message = "City is required")
        @Size(max = 120)
        String city,

        @NotBlank(message = "FSSAI licence number is required")
        @Pattern(regexp = "^\\d{14}$",
                 message = "FSSAI licence number must contain 14 digits")
        String fssaiNumber,

        @Size(max = 1000)
        String image,

        @Size(max = 100)
        String fssaiDocumentFileName,

        @Size(max = 100)
        String gstDocumentFileName,

        @NotBlank(message = "Bank account number is required")
        @Size(max = 34)
        String bankAccountNumber,

        @NotBlank(message = "IFSC code is required")
        @Pattern(regexp = "^[A-Za-z]{4}0[A-Za-z0-9]{6}$",
                 message = "Enter a valid IFSC code")
        String bankIfscCode,

        @NotBlank(message = "Account holder name is required")
        @Size(max = 120)
        String bankAccountHolderName
) {
}
