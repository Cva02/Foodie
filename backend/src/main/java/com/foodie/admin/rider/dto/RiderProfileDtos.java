package com.foodie.admin.rider.dto;

public final class RiderProfileDtos {

    private RiderProfileDtos() {}

    public record RiderProfileView(
        String id,
        String name,
        String email,
        String phone,
        String vehicle,
        String vehicleNumber,
        String licenseNumber,
        String city,
        String servicePinCodes,
        String status,
        double rating,
        int totalDeliveries,
        double earnings
) {}

    public record UpdateRiderProfileRequest(
            String name,
            String phone,
            String vehicle,
            String vehicleNumber,
            String licenseNumber,
            String city,
            String servicePinCodes
    ) {}
}