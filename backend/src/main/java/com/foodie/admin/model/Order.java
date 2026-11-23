package com.foodie.admin.model;

public record Order(
        String id,
        String customerId,
        String customerName,
        String sellerId,
        String sellerName,
        String riderId,         // null until a rider is assigned
        String riderName,
        String customerPhone,
        String items,
        double amount,
        String status,          // PLACED | ACCEPTED | READY | RIDER_ASSIGNED | PICKED_UP | DISPATCHED | DELIVERED | REJECTED
        String payment,
        String sellerAddress,
        String address,
        String customerPinCode,
        String placedAt,
        String expectedDeliveryAt,
        String deliveredAt,
        Double distanceKm,
        Double sellerLatitude,
        Double sellerLongitude,
        Double customerLatitude,
        Double customerLongitude) {

    public Order withCustomerPhone(String phone) {
        return new Order(id, customerId, customerName, sellerId, sellerName, riderId, riderName,
                phone, items, amount, status, payment, sellerAddress, address, customerPinCode, placedAt,
                expectedDeliveryAt, deliveredAt, distanceKm, sellerLatitude, sellerLongitude,
                customerLatitude, customerLongitude);
    }
}
