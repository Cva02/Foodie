package com.foodie.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminPasswordResetConfirmRequest(
        @NotBlank String requestId,
        @NotBlank @Size(max = 60) String username,
        @NotBlank @Pattern(regexp = "^[0-9]{4,8}$") String otp,
        @NotBlank @Size(min = 8, max = 100) String newPassword) {
}
