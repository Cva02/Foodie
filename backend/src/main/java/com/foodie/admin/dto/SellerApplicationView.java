package com.foodie.admin.dto;

import java.time.Instant;

public record SellerApplicationView(
        String id,
        String restaurantName,
        String ownerName,
        String cuisine,
        String phone,
        String email,
        String address,
        String city,
        String fssaiNumber,
        String image,
        boolean hasFssaiDocument,
        String status,
        String reviewNote,
        String sellerId,
        Instant submittedAt) {
}
