
package com.foodie.admin.rider.dto;

public final class RiderOrderDtos {

    private RiderOrderDtos() {}

    public record RiderOrderView(
            Long id,
            String sellerName,
            String status,
            int amount,
            String items,
            String placedAt,
            String customerPinCode,
            Double distanceKm,
            String riderId,
            String payment,
            String expectedDeliveryAt,
            String deliveredAt,
            String sellerAddress,
            String address,
            String customerName,
            String customerPhone,
            Double sellerLatitude,
            Double sellerLongitude,
            Double customerLatitude,
            Double customerLongitude
    ) {}
}
