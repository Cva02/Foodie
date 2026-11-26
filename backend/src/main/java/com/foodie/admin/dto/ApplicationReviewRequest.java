package com.foodie.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplicationReviewRequest(
        @NotBlank(message = "A reason is required")
        @Size(max = 1000, message = "The reason must be 1000 characters or fewer")
        String note) {
}
