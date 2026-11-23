package com.foodie.admin.dto;

import java.time.Instant;
import java.util.List;

public record DeliveryTracking(
        String orderId,
        String customerName,
        String sellerName,
        String sellerAddress,
        Double sellerLatitude,
        Double sellerLongitude,
        String customerAddress,
        String customerPinCode,
        Double customerLatitude,
        Double customerLongitude,
        String itemName,
        String status,
        String riderName,
        Instant orderedAt,
        Instant expectedDeliveryAt,
        Instant deliveredAt,
        boolean demoOrder,
        String pickupOtp,
        String deliveryOtp,
        List<DeliveryPoint> route) {
}
