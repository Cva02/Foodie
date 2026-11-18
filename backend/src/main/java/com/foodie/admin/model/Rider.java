package com.foodie.admin.model;

import java.util.List;

public record Rider(
        String id,
        String name,
        String avatar,
        String phone,
        String email,
        String vehicle,
        String vehicleNumber,
        String licenseNumber,
        String city,
        String status,          // ONLINE | ON_DELIVERY | OFFLINE | PENDING | REJECTED | SUSPENDED
        double rating,
        int totalDeliveries,
        double earnings,
        String joinedOn,
        List<RiderDocument> documents) {

    public Rider withStatus(String newStatus) {
        return new Rider(id, name, avatar, phone, email, vehicle, vehicleNumber, licenseNumber, city,
                newStatus, rating, totalDeliveries, earnings, joinedOn, documents);
    }

    public Rider withProfile(RiderProfileUpdate profile) {
        return new Rider(id, profile.name(), avatar, profile.phone(), profile.email(), profile.vehicle(),
                profile.vehicleNumber(), profile.licenseNumber(), profile.city(), status, rating,
                totalDeliveries, earnings, joinedOn, documents);
    }
}
