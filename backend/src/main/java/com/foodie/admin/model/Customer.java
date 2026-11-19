package com.foodie.admin.model;

public record Customer(
        String id,
        String name,
        String email,
        String phone,
        String avatar,
        String city,
        String address,
        String joinedOn,
        String status,          // ACTIVE | BLOCKED
        int totalOrders,
        double totalSpent) {

    public Customer withStatus(String newStatus) {
        return new Customer(id, name, email, phone, avatar, city, address, joinedOn,
                newStatus, totalOrders, totalSpent);
    }
}
