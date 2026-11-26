package com.foodie.admin.model;

public record Order(
        String id,
        String customerId,
        String customerName,
        String sellerId,
        String sellerName,
        String riderId,         // null until a rider is assigned
        String riderName,
        String items,
        double amount,
        String status,          // AWAITING_SELLER | PREPARING | AWAITING_RIDER | ON_THE_WAY | DELIVERED | CANCELLED
        String payment,         // UPI | CARD | CASH ON DELIVERY
        String address,
        String placedAt) {
}
