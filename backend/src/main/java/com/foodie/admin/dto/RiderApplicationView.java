package com.foodie.admin.dto;

import java.time.Instant;

public record RiderApplicationView(
        String id,
        String name,
        String phone,
        String email,
        String vehicle,
        String vehicleNumber,
        String licenseNumber,
        String city,
        String photoUrl,
        boolean hasLicenseDocument,
        String status,
        String reviewNote,
        String riderId,
        Instant submittedAt) {
}
