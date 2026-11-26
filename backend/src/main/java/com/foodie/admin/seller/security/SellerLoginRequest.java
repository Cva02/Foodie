
package com.foodie.admin.seller.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SellerLoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password

) {
}
