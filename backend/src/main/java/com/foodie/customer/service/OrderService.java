package com.foodie.customer.service;

import com.foodie.admin.repository.RiderRepository;
import com.foodie.customer.dto.Dtos.*;
import com.foodie.customer.model.*;
import com.foodie.customer.repo.Repos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Set<String> PAYMENTS = Set.of("COD", "UPI", "CARD");

    private final OrderRepo orders;
    private final RestaurantRepo restaurants;
    private final MenuItemRepo menu;
    private final RiderRepository riders;
    private final NotificationService notifications;

    @Transactional
    public OrderDto place(Long uid, PlaceOrderReq req) {
        Restaurant r = restaurants.findById(req.restaurantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
        if (!r.isOpen())
            throw new ResponseStatusException(HttpStatus.CONFLICT, r.getName() + " is closed right now");

        Map<Long, MenuItem> byId = menu.findByRestaurantId(r.getId()).stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));

        OrderEntity o = new OrderEntity();
        o.setUserId(uid);
        o.setRestaurantId(r.getId());
        o.setRestaurantName(r.getName());
        o.setAddress(req.address().trim());
        o.setPinCode(req.pinCode().trim());
        o.setLat(req.lat());
        o.setLon(req.lon());
        o.setInstructions(req.instructions());
        String pay = req.paymentMethod() == null ? "COD" : req.paymentMethod().toUpperCase();
        o.setPaymentMethod(PAYMENTS.contains(pay) ? pay : "COD");

        int itemTotal = 0;
        for (OrderLine line : req.items()) {
            MenuItem mi = byId.get(line.menuItemId());
            if (mi == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An item in your cart is not on this restaurant's menu");
            if (!mi.isAvailable())
                throw new ResponseStatusException(HttpStatus.CONFLICT, mi.getName() + " is currently unavailable");
            OrderItem oi = new OrderItem();
            oi.setOrder(o);
            oi.setMenuItemId(mi.getId());
            oi.setName(mi.getName());
            oi.setPrice(mi.getPrice());
            oi.setQty(line.qty());
            oi.setVeg(mi.isVeg());
            o.getItems().add(oi);
            itemTotal += mi.getPrice() * line.qty();
        }
        o.setItemTotal(itemTotal);
        o.setDeliveryFee(0);
        CouponQuote coupon = couponQuote(uid, req.couponCode(), itemTotal);
        int discount = Math.max(0, Math.min(coupon.discount(), itemTotal));
        int discountedSubtotal = Math.max(0, itemTotal - discount);
        int taxes = Math.round(discountedSubtotal * 0.05f);
        o.setDiscount(discount);
        o.setCouponCode(coupon.couponCode());
        o.setTaxes(taxes);
        o.setTotal(Math.max(0, discountedSubtotal + o.getDeliveryFee() + taxes));
        o = orders.save(o);

        notifications.toUser(uid, "ORDER_PLACED", "Order placed",
                "Your order #" + o.getId() + " from " + r.getName() + " has been placed. Total ₹" + o.getTotal() + ".", o.getId());
        notifications.toRestaurant(r.getId(), "NEW_ORDER", "New order #" + o.getId(),
                o.getItems().size() + " item(s), total ₹" + o.getTotal() + ". Please confirm and start preparing.", o.getId());
        return toDto(o);
    }

    @Transactional(readOnly = true)
    public CouponQuote validateCoupon(Long uid, String couponCode, Long restaurantId, List<OrderLine> lines) {
        Restaurant restaurant = restaurants.findById(restaurantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
        Map<Long, MenuItem> byId = menu.findByRestaurantId(restaurant.getId()).stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));

        int itemTotal = 0;
        for (OrderLine line : lines) {
            MenuItem item = byId.get(line.menuItemId());
            if (item == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "An item in your cart is not on this restaurant's menu");
            if (!item.isAvailable())
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        item.getName() + " is currently unavailable");
            itemTotal += item.getPrice() * line.qty();
        }
        return couponQuote(uid, couponCode, itemTotal);
    }

    private CouponQuote couponQuote(Long uid, String couponCode, int itemTotal) {
        if (couponCode == null || couponCode.isBlank()) return new CouponQuote(null, itemTotal, 0);

        String code = couponCode.trim().toUpperCase(Locale.ROOT);
        int orderTotalWithTax = itemTotal + Math.round(itemTotal * 0.05f);
        int discount;
        switch (code) {
            case "WELCOME100" -> {
                if (itemTotal < 399 && orderTotalWithTax < 399)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WELCOME100 requires an order of at least ₹399");
                discount = Math.min(100, itemTotal);
            }
            case "FOODIE50" -> {
                if (itemTotal < 299 && orderTotalWithTax < 299)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FOODIE50 requires an order of at least ₹299");
                discount = Math.min(50, itemTotal);
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid coupon code");
        }
        return new CouponQuote(code, itemTotal, discount);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> list(Long uid) {
        return orders.findByUserIdOrderByIdDesc(uid).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public OrderDto get(Long uid, Long id) {
        return toDto(owned(uid, id));
    }

    @Transactional
    public OrderDto cancel(Long uid, Long id, String reason) {
        OrderEntity o = owned(uid, id);
        if (!o.getStatus().cancellable()) {
            String why = o.getStatus() == OrderStatus.CANCELLED ? "This order is already cancelled"
                    : "This order can no longer be cancelled because it is " + o.getStatus().name().toLowerCase().replace('_', ' ');
            throw new ResponseStatusException(HttpStatus.CONFLICT, why);
        }
        o.setStatusNow(OrderStatus.CANCELLED);
        o.setCancelledAt(Instant.now());
        o.setCancelReason(reason == null || reason.isBlank() ? "Cancelled by customer" : reason.trim());
        notifications.toUser(uid, "ORDER_CANCELLED", "Order cancelled",
                "Your order #" + o.getId() + " from " + o.getRestaurantName() + " has been cancelled."
                        + (!"COD".equals(o.getPaymentMethod()) ? " Any amount paid will be refunded in 3-5 working days." : ""), o.getId());
        notifications.toRestaurant(o.getRestaurantId(), "ORDER_CANCELLED", "Order #" + o.getId() + " cancelled",
                "The customer cancelled this order. Please stop preparing it.", o.getId());
        return toDto(o);
    }

    @Transactional
    public OrderDto rateRider(Long uid, Long id, Integer rating) {
        OrderEntity o = orders.findByIdForUpdate(id)
                .filter(order -> order.getUserId().equals(uid))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (o.getStatus() != OrderStatus.DELIVERED || o.getRiderId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You can rate the rider after your order is delivered");
        }
        if (o.getRiderRating() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already rated this rider");
        }

        o.setRiderRating(rating);
        return toDto(o);
    }

    private OrderEntity owned(Long uid, Long id) {
        return orders.findById(id).filter(o -> o.getUserId().equals(uid))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    OrderDto toDto(OrderEntity o) {
        RiderDto rider = o.getRiderId() == null ? null : riders.findById(o.getRiderId())
                .map(x -> new RiderDto(x.getName(), x.getPhone(), x.getVehicle())).orElse(null);
        List<OrderItemDto> items = o.getItems().stream()
                .map(i -> new OrderItemDto(i.getMenuItemId(), i.getName(), i.getPrice(), i.getQty(), i.isVeg())).toList();
        return new OrderDto(
        o.getId(),
        o.getRestaurantId(),
        o.getRestaurantName(),
        o.getStatus().name(),
        items,
        o.getItemTotal(),
        o.getDeliveryFee(),
        o.getTaxes(),
        o.getDiscount(),
        o.getCouponCode(),
        o.getTotal(),
        o.getPaymentMethod(),
        o.getAddress(),
        o.getDeliveryCode(),
        o.getCreatedAt(),
        o.getStatusUpdatedAt(),
        rider,
        o.getStatus().cancellable(),
        o.getCancelReason(),
        o.getRiderRating()
);
    }
}
