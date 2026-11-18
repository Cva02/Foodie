package com.foodie.admin.model;

public record RiderProfileUpdate(
        String name,
        String phone,
        String email,
        String city,
        String vehicle,
        String vehicleNumber,
        String licenseNumber) {
}
