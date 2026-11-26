
package com.foodie.admin.seller.dto;

public final class SellerProfileDtos {

    private SellerProfileDtos() {}

    public record ProfileView(
            String id,
            String restaurantName,
            String ownerName,
            String email,
            String phone,
            String cuisine,
            String address,
            String city,
            String fssaiNumber,
            String restaurantImage,
            String status,
            String statusNote,
            boolean open
    ) {}

    public record AvailabilityRequest(boolean open) {}
}
