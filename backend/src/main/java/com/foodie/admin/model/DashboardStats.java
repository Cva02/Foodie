package com.foodie.admin.model;

import java.util.List;

public record DashboardStats(
        int customers,
        int sellers,
        int riders,
        int ridersOnline,
        int pendingApprovals,
        int totalOrders,
        int liveOrders,
        double revenue,
        List<Order> recentOrders) {
}
