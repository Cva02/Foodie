package com.foodie.admin.dto;

import java.time.Instant;

public record RiderSupportTicketResponse(
        Long id,
        String subject,
        String message,
        String status,
        Instant createdAt) {
}
