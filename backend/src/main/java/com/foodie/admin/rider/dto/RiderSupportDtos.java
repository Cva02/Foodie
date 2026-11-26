package com.foodie.admin.rider.dto;

import java.time.Instant;

public final class RiderSupportDtos {

    private RiderSupportDtos() {}

    public record RiderSupportRequest(
            String subject,
            String message
    ) {}

    public record RiderSupportView(
            Long id,
            String subject,
            String message,
            String status,
            Instant createdAt
    ) {}
}