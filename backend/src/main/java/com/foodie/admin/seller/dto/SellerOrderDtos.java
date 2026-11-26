
package com.foodie.admin.seller.dto;

import java.time.Instant;
import java.util.List;

public final class SellerOrderDtos {

    private SellerOrderDtos() {}

    public record OrderItemView(
            String name,
            int quantity,
            int price
    ) {}

    public record OrderView(
            Long id,
            String orderNumber,
            String customerName,
            String status,
            String paymentMode,
            Instant createdAt,
            List<OrderItemView> items,
            int total,
            String pickupCode,
            String cancelReason
    ) {}

    public record StatusRequest(
            String status,
            String reason
    ) {}

        public record SummaryView(
            long totalOrders,
            long pendingOrders,
            long activeOrders,
            long completedOrders,
            long cancelledOrders,
            long grossSales,
            long discounts,
            long estimatedEarnings
    ) {}
}
