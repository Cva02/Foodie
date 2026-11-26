
package com.foodie.admin.seller.service;

import com.foodie.admin.model.Seller;
import com.foodie.admin.repository.SellerRepository;
import com.foodie.admin.seller.dto.SellerOrderDtos.SummaryView;
import com.foodie.customer.model.OrderEntity;
import com.foodie.customer.model.OrderStatus;
import com.foodie.customer.repo.Repos.OrderRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerSummaryService {

    private final SellerRepository sellers;
    private final OrderRepo orders;

    public SellerSummaryService(
            SellerRepository sellers,
            OrderRepo orders) {
        this.sellers = sellers;
        this.orders = orders;
    }

    public SummaryView getSummary(String sellerId) {
        Seller seller = sellers.findById(sellerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Seller not found"));

        if (!"ACTIVE".equalsIgnoreCase(seller.getStatus())) {
            throw new IllegalStateException(
                    "Seller account is not active");
        }

        if (seller.getRestaurantId() == null) {
            throw new IllegalStateException(
                    "No restaurant is linked to this seller");
        }

        List<OrderEntity> restaurantOrders =
                orders.findByRestaurantIdOrderByIdDesc(
                        seller.getRestaurantId());

        long total = restaurantOrders.size();

        long pending = restaurantOrders.stream()
                .filter(order ->
                        order.getStatus() == OrderStatus.PLACED)
                .count();

        long active = restaurantOrders.stream()
                .filter(order -> order.getStatus() != null
                        && order.getStatus().active()
                        && order.getStatus() != OrderStatus.PLACED)
                .count();

        List<OrderEntity> deliveredOrders = restaurantOrders.stream()
                .filter(order ->
                        order.getStatus() == OrderStatus.DELIVERED)
                .toList();

        long completed = deliveredOrders.size();

        long cancelled = restaurantOrders.stream()
                .filter(order ->
                        order.getStatus() == OrderStatus.CANCELLED)
                .count();

        long grossSales = deliveredOrders.stream()
                .mapToLong(OrderEntity::getItemTotal)
                .sum();

        long discounts = deliveredOrders.stream()
                .mapToLong(OrderEntity::getDiscount)
                .sum();

        long estimatedEarnings = Math.max(0, grossSales - discounts);

        return new SummaryView(
                total,
                pending,
                active,
                completed,
                cancelled,
                grossSales,
                discounts,
                estimatedEarnings
        );
    }
}
