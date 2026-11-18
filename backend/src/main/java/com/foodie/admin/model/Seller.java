package com.foodie.admin.model;

import java.util.List;

public record Seller(
        String id,
        String name,            // restaurant name
        String ownerName,
        String ownerAvatar,
        String image,           // cover image
        String cuisine,
        String phone,
        String email,
        String address,
        String city,
        String fssaiNumber,
        double rating,
        String status,          // ACTIVE | PENDING | SUSPENDED
        boolean open,
        String joinedOn,
        int totalOrders,
        double revenue,
        List<MenuItem> menu) {

    public Seller withStatus(String newStatus) {
        return new Seller(id, name, ownerName, ownerAvatar, image, cuisine, phone, email, address,
                city, fssaiNumber, rating, newStatus, open, joinedOn, totalOrders, revenue, menu);
    }

    public Seller withProfile(SellerProfileUpdate profile) {
        return new Seller(id, profile.name(), profile.ownerName(), ownerAvatar, image, profile.cuisine(),
                profile.phone(), profile.email(), profile.address(), profile.city(), profile.fssaiNumber(),
                rating, status, open, joinedOn, totalOrders, revenue, menu);
    }
}
