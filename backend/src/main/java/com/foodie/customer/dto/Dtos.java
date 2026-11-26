package com.foodie.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record SignupReq(@NotBlank String name, @NotBlank @Email String email, @NotBlank String phone,
                            @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password) {}
    public record LoginReq(@NotBlank String email, @NotBlank String password) {}
    public record UserDto(Long id, String name, String email, String phone, Double lat, Double lon, String locationLabel) {}
    public record AuthRes(String token, UserDto user) {}
    public record ProfileReq(@NotBlank String name, String phone) {}
    public record PasswordReq(@NotBlank String currentPassword,
                              @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String newPassword) {}
    public record LocationReq(Double lat, Double lon, @NotBlank String label) {}
    public record AddressReq(
        String label,
        @NotBlank String line,
        @Pattern(regexp = "^$|^[0-9]{6}$", message = "PIN code must contain 6 digits")
        String pinCode,
        Double lat,
        Double lon
) {}

public record AddressDto(
        Long id,
        String label,
        String line,
        String pinCode,
        Double lat,
        Double lon
) {}

    public record RestaurantDto(Long id, String name, String cuisines, String emoji, String imageUrl, double rating,
                                int deliveryMinutes, int costForTwo, boolean pureVeg, String area, boolean open,
                                String offer, Double distanceKm) {}
    public record MenuItemDto(Long id, String name, String description, int price, boolean veg, String category,
                              boolean bestseller, boolean available, String emoji, String imageUrl) {}
    public record RestaurantDetail(RestaurantDto restaurant, List<MenuItemDto> items) {}

    public record OrderLine(@NotNull Long menuItemId, @Min(1) @Max(20) int qty) {}
    public record CouponReq(@NotBlank @Size(max = 32) String couponCode, @NotNull Long restaurantId,
                            @NotEmpty List<@Valid OrderLine> items) {}
    public record CouponQuote(String couponCode, int itemTotal, int discount) {}
    public record PlaceOrderReq(
        @NotNull Long restaurantId,
        @NotEmpty List<@Valid OrderLine> items,
        @NotBlank String address,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "A valid 6-digit delivery PIN is required")
        String pinCode,
        Double lat,
        Double lon,
        String paymentMethod,
        String instructions,
        @Size(max = 32) String couponCode
) {}
    public record CancelReq(String reason) {}
    public record RiderRatingReq(@NotNull @Min(1) @Max(5) Integer rating) {}
    public record OrderItemDto(Long menuItemId, String name, int price, int qty, boolean veg) {}
    public record RiderDto(String name, String phone, String vehicle) {}
    public record OrderDto(
        Long id,
        Long restaurantId,
        String restaurantName,
        String status,
        List<OrderItemDto> items,
        int itemTotal,
        int deliveryFee,
        int taxes,
        int discount,
        String couponCode,
        int total,
        String paymentMethod,
        String address,
        String deliveryCode,
        Instant createdAt,
        Instant statusUpdatedAt,
        RiderDto rider,
        boolean cancellable,
        String cancelReason,
        Integer riderRating
) {}
    public record NotificationDto(Long id, String type, String title, String message, boolean read, Long orderId,
                                  Instant createdAt) {}
}
