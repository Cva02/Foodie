package com.foodie.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RiderLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {
}
