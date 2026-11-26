package com.foodie.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminPasswordResetRequest(
        @NotBlank @Size(max = 60) String username) {
}
