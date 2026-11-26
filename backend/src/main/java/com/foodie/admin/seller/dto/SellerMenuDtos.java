package com.foodie.admin.seller.dto;

public final class SellerMenuDtos {

    private SellerMenuDtos() {}

    public record MenuItemRequest(
            String name,
            String description,
            int price,
            boolean veg,
            String category,
            boolean bestseller,
            boolean inStock,
            String imageUrl,
            String emoji
    ) {}

    public record MenuItemView(
            Long id,
            String name,
            String description,
            int price,
            boolean veg,
            String category,
            boolean bestseller,
            boolean inStock,
            String imageUrl,
            String emoji
    ) {}

    public record ImageUploadResponse(String url) {}
}