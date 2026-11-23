package com.foodie.admin.dto;

import java.time.Instant;

public record RiderNotificationResponse(
        Long id,
        String orderId,
        String type,
        String message,
        Instant createdAt,
        Instant readAt) {
}
