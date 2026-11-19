package com.foodie.admin.dto;

import jakarta.validation.constraints.Size;

public record StatusNoteRequest(
        @Size(max = 1000, message = "The reason must be 1000 characters or fewer")
        String note) {
}
