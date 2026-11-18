package com.foodie.admin.model;

public record CustomerProfileUpdate(
        String name,
        String email,
        String phone,
        String city,
        String address) {
}
