package com.foodie.admin.dto;

import java.time.Instant;

public record DeliveryPoint(double latitude, double longitude, Instant recordedAt) {
}
