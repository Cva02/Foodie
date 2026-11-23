package com.foodie.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RiderSupportRequest(
        @NotBlank @Size(max = 120) String subject,
        @NotBlank @Size(max = 2000) String message) {
}
