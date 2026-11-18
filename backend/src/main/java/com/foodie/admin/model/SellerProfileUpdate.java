package com.foodie.admin.model;

public record SellerProfileUpdate(
        String name,
        String ownerName,
        String phone,
        String email,
        String cuisine,
        String address,
        String city,
        String fssaiNumber) {
}
