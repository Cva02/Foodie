package com.foodie.admin.seller.service;

import com.foodie.admin.model.Seller;
import com.foodie.admin.repository.SellerRepository;
import com.foodie.customer.model.OrderEntity;
import com.foodie.customer.model.OrderStatus;
import com.foodie.customer.model.User;
import com.foodie.customer.repo.Repos.OrderRepo;
import com.foodie.customer.repo.Repos.UserRepo;
import com.foodie.customer.service.NotificationService;
import com.foodie.admin.seller.dto.SellerOrderDtos.OrderItemView;
import com.foodie.admin.seller.dto.SellerOrderDtos.OrderView;
import com.foodie.admin.seller.dto.SellerOrderDtos.StatusRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SellerOrderService {

    private final SellerRepository sellers;
    private final OrderRepo orders;
    private final UserRepo users;
    private final NotificationService notifications;

    public SellerOrderService(
            SellerRepository sellers,
            OrderRepo orders,
            UserRepo users,
            NotificationService notifications) {
        this.sellers = sellers;
        this.orders = orders;
        this.users = users;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<OrderView> listOrders(String sellerId, String group) {
        Seller seller = activeSeller(sellerId);

        List<OrderEntity> restaurantOrders =
                orders.findByRestaurantIdOrderByIdDesc(seller.getRestaurantId());

        List<OrderEntity> filtered = filterOrders(restaurantOrders, group);

        Set<Long> userIds = filtered.stream()
                .map(OrderEntity::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> customerMap = users.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return filtered.stream()
                .map(order -> toView(order, customerMap.get(order.getUserId())))
                .toList();
    }

    @Transactional
    public OrderView updateStatus(String sellerId, Long orderId, StatusRequest request) {
        Seller seller = activeSeller(sellerId);

        if (request == null || request.status() == null || request.status().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order status is required");
        }

        OrderEntity order = orders.findById(orderId)
                .filter(o -> seller.getRestaurantId().equals(o.getRestaurantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        String action = request.status().trim().toUpperCase();

        switch (action) {
            case "ACCEPTED" -> {
                requireStatus(order, OrderStatus.PLACED);
                order.setStatusNow(OrderStatus.CONFIRMED);
                notifications.toUser(order.getUserId(), "ORDER_CONFIRMED",
                        "Order accepted",
                        "Your order #" + order.getId() + " has been accepted by the restaurant.",
                        order.getId());
            }
            case "REJECTED" -> {
                requireStatus(order, OrderStatus.PLACED);
                String reason = request.reason() == null || request.reason().isBlank()
                        ? "Rejected by restaurant"
                        : request.reason().trim();

                order.setStatusNow(OrderStatus.CANCELLED);
                order.setCancelledAt(Instant.now());
                order.setCancelReason("Rejected by restaurant: " + reason);

                notifications.toUser(order.getUserId(), "ORDER_CANCELLED",
                        "Order rejected",
                        "Your order #" + order.getId() + " was rejected by the restaurant. Reason: " + reason,
                        order.getId());
            }
            case "PREPARING" -> {
                requireStatus(order, OrderStatus.CONFIRMED);
                order.setStatusNow(OrderStatus.PREPARING);
                notifications.toUser(order.getUserId(), "ORDER_PREPARING",
                        "Order being prepared",
                        "The restaurant has started preparing your order #" + order.getId() + ".",
                        order.getId());
            }
            case "READY_FOR_PICKUP" -> {
    requireStatus(order, OrderStatus.PREPARING);

    java.security.SecureRandom random = new java.security.SecureRandom();

int pickupCode = 100000 + random.nextInt(900000);
int deliveryCode = 100000 + random.nextInt(900000);

order.setPickupCode(String.valueOf(pickupCode));
order.setDeliveryCode(String.valueOf(deliveryCode));
order.setStatusNow(OrderStatus.OUT_FOR_DELIVERY);
order.setRiderDeliveryStage("READY");
    

    

    notifications.toUser(order.getUserId(), "ORDER_READY",
            "Order ready for pickup",
            "Your order #" + order.getId() + " is ready for rider pickup.",
            order.getId());
}
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Unsupported Seller order status: " + action);
        }

        OrderEntity saved = orders.save(order);
        User customer = users.findById(saved.getUserId()).orElse(null);
        return toView(saved, customer);
    }

    private Seller activeSeller(String sellerId) {
        Seller seller = sellers.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));

        if (!"ACTIVE".equalsIgnoreCase(seller.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller account is not active");
        }

        if (seller.getRestaurantId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Your seller account is not linked to a restaurant yet");
        }

        return seller;
    }

    private void requireStatus(OrderEntity order, OrderStatus expected) {
        if (order.getStatus() != expected) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This order cannot be updated from " + order.getStatus());
        }
    }

    private List<OrderEntity> filterOrders(List<OrderEntity> source, String group) {
        String selected = group == null || group.isBlank()
                ? "all" : group.trim().toLowerCase();

        return switch (selected) {
            case "pending" -> source.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PLACED)
                    .toList();
            case "active" -> source.stream()
                    .filter(o -> o.getStatus().active()
                            && o.getStatus() != OrderStatus.PLACED)
                    .toList();
            case "queue" -> source.stream()
                    .filter(o -> o.getStatus().active())
                    .toList();
            case "history" -> source.stream()
                    .filter(o -> !o.getStatus().active())
                    .toList();
            case "all" -> source;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid order group: " + selected);
        };
    }

    private OrderView toView(OrderEntity order, User customer) {
        List<OrderItemView> items = order.getItems().stream()
                .map(item -> new OrderItemView(
                        item.getName(),
                        item.getQty(),
                        item.getPrice()))
                .toList();

        return new OrderView(
                order.getId(),
                "FD" + (10000 + order.getId()),
                customer == null ? "Customer" : customer.getName(),
                sellerStatus(order),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                items,
                order.getTotal(),
                "READY_FOR_PICKUP".equals(sellerStatus(order))
        ? order.getPickupCode()
        : null,
                order.getCancelReason()
        );
    }

    private String sellerStatus(OrderEntity order) {
    return switch (order.getStatus()) {
        case PLACED -> "PENDING";
        case CONFIRMED -> "ACCEPTED";
        case PREPARING -> "PREPARING";
        case OUT_FOR_DELIVERY -> "READY_FOR_PICKUP";
        case DELIVERED -> "DELIVERED";
        case CANCELLED ->
                order.getCancelReason() != null
                        && order.getCancelReason().startsWith("Rejected by restaurant")
                        ? "REJECTED"
                        : "CANCELLED";
    };
}

}
