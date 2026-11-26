
package com.foodie.admin.rider.service;

import com.foodie.admin.model.Rider;
import com.foodie.admin.model.RiderOrderDecline;
import com.foodie.admin.repository.RiderRepository;
import com.foodie.admin.repository.RiderOrderDeclineRepository;
import com.foodie.admin.repository.SellerRepository;
import com.foodie.admin.rider.dto.RiderOrderDtos.RiderOrderView;
import com.foodie.customer.model.*;
import com.foodie.customer.repo.Repos.OrderRepo;
import com.foodie.customer.repo.Repos.RestaurantRepo;
import com.foodie.customer.repo.Repos.UserRepo;
import com.foodie.customer.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RiderOrderService {

    private final OrderRepo orderRepo;
    private final RestaurantRepo restaurantRepo;
    private final UserRepo userRepo;
    private final RiderRepository riderRepository;
    private final SellerRepository sellerRepository;
    private final NotificationService notifications;
    private final RiderOrderDeclineRepository declineRepository;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a",
                    Locale.ENGLISH
            ).withZone(ZoneId.systemDefault());

    @Transactional(readOnly = true)
    public List<RiderOrderView> getOrders(String riderId) {

        Rider rider = getRider(riderId);

        return orderRepo.findAll().stream()
                .filter(order -> {

                    boolean available =
                            "READY".equals(order.getRiderDeliveryStage())
                            && order.getRiderId() == null
                            && order.getStatus() == OrderStatus.OUT_FOR_DELIVERY
                            && "ONLINE".equals(rider.getStatus())
                            && rider.servesPinCode(order.getPinCode())
                            && !declineRepository.existsByRiderIdAndOrderId(
                                    riderId,
                                    order.getId()
                            );

                    boolean assigned =
                            Objects.equals(order.getRiderId(), riderId);

                    return available || assigned;
                })
                .map(order -> toView(order, rider))
                .toList();
    }

    @Transactional
    public String acceptOrder(String riderId, Long orderId) {

        Rider rider = getRider(riderId);

        if (!"ONLINE".equals(rider.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You must be online to accept an order."
            );
        }

        OrderEntity order = orderRepo.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found."
                ));

        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY
                || !"READY".equals(order.getRiderDeliveryStage())
                || order.getRiderId() != null) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This order is no longer available."
            );
        }

        if (!rider.servesPinCode(order.getPinCode())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This order is outside your service PIN codes."
            );
        }

        order.setRiderId(riderId);
        order.setRiderDeliveryStage("RIDER_ASSIGNED");
        rider.setStatus("ON_DELIVERY");

        orderRepo.save(order);
        riderRepository.save(rider);

        notifications.toRider(
                riderId,
                "ORDER_ACCEPTED",
                "Order accepted",
                "You have accepted order #" + order.getId() + ".",
                order.getId()
        );

        notifications.toUser(
                order.getUserId(),
                "ORDER_DISPATCHED",
                "Rider assigned",
                rider.getName() + " has been assigned to deliver your order #" + order.getId() + ".",
                order.getId()
        );

        return "Delivery accepted successfully.";
    }

    @Transactional
    public String performAction(
            String riderId,
            Long orderId,
            String action,
            String otp
    ) {

        // Decline an available order.
        if ("decline".equalsIgnoreCase(action)) {

            Rider rider = getRider(riderId);

            if (!"ONLINE".equals(rider.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "You must be online to decline an order."
                );
            }

            OrderEntity order = orderRepo.findByIdForUpdate(orderId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Order not found."
                    ));

            if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY
                    || !"READY".equals(order.getRiderDeliveryStage())
                    || order.getRiderId() != null) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This order is no longer available."
                );
            }

            if (!rider.servesPinCode(order.getPinCode())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "This order is outside your service PIN codes."
                );
            }

            if (declineRepository.existsByRiderIdAndOrderId(
                    riderId,
                    orderId
            )) {
                return "Order was already declined.";
            }

            declineRepository.save(
                    new RiderOrderDecline(riderId, orderId)
            );

            return "Order declined for you. It remains available to other riders.";
        }

        // All other actions require the order to be assigned to this rider.
        OrderEntity order = orderRepo.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found."
                ));

        if (!Objects.equals(order.getRiderId(), riderId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This order is not assigned to you."
            );
        }

        Rider rider = getRider(riderId);

        switch (action) {

            case "picked-up" -> {

                if (!"RIDER_ASSIGNED".equals(
                        order.getRiderDeliveryStage()
                )) {
                    throw invalidStage();
                }

                verifyOtp(
                        otp,
                        order.getPickupCode(),
                        "pickup"
                );

                order.setRiderDeliveryStage("PICKED_UP");
                notifications.toUser(
                        order.getUserId(),
                        "ORDER_DISPATCHED",
                        "Order picked up",
                        rider.getName() + " picked up your order #" + order.getId() + " from the restaurant.",
                        order.getId()
                );
            }

            case "dispatched" -> {

                if (!"PICKED_UP".equals(
                        order.getRiderDeliveryStage()
                )) {
                    throw invalidStage();
                }

                order.setRiderDeliveryStage("DISPATCHED");
                notifications.toUser(
                        order.getUserId(),
                        "ORDER_DISPATCHED",
                        "Order on the way",
                        rider.getName() + " is on the way with your order #" + order.getId() + ".",
                        order.getId()
                );
            }

            case "delivered" -> {

                if (!"DISPATCHED".equals(
                        order.getRiderDeliveryStage()
                )) {
                    throw invalidStage();
                }

                verifyOtp(
                        otp,
                        order.getDeliveryCode(),
                        "delivery"
                );

                order.setRiderDeliveryStage("DELIVERED");
                order.setStatusNow(OrderStatus.DELIVERED);

                rider.setStatus("ONLINE");
                rider.setTotalDeliveries(rider.getTotalDeliveries() + 1);
                rider.setEarnings(rider.getEarnings() + 50.0);
                riderRepository.save(rider);

                sellerRepository.findByRestaurantId(order.getRestaurantId()).ifPresent(seller -> {
                    seller.setTotalOrders(seller.getTotalOrders() + 1);
                    seller.setRevenue(seller.getRevenue() + order.getTotal());
                    sellerRepository.save(seller);
                });

                notifications.toRider(
                        riderId,
                        "ORDER_DELIVERED",
                        "Delivery completed",
                        "You successfully delivered order #"
                                + order.getId() + ".",
                        order.getId()
                );

                notifications.toUser(
                        order.getUserId(),
                        "ORDER_DELIVERED",
                        "Order delivered",
                        "Your order #" + order.getId() + " has been delivered. Enjoy your meal!",
                        order.getId()
                );
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid rider action."
            );
        }

        orderRepo.save(order);

        return "Order updated successfully.";
    }

    private void verifyOtp(
            String submitted,
            String expected,
            String type
    ) {

        if (submitted == null
                || !submitted.matches("\\d{6}")
                || expected == null
                || !expected.equals(submitted)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid " + type + " OTP."
            );
        }
    }

    private ResponseStatusException invalidStage() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This action is not allowed for the current order status."
        );
    }

    private Rider getRider(String riderId) {
        return riderRepository.findById(riderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rider not found."
                ));
    }

    private RiderOrderView toView(
            OrderEntity order,
            Rider rider
    ) {

        Restaurant restaurant = restaurantRepo.findById(
                order.getRestaurantId()
        ).orElse(null);

        User customer = userRepo.findById(
                order.getUserId()
        ).orElse(null);

        String items = order.getItems().stream()
                .map(item -> item.getName() + " x " + item.getQty())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        Double distance = null;

        if (rider.getLatitude() != null
                && rider.getLongitude() != null
                && restaurant != null) {

            distance = distanceKm(
                    rider.getLatitude(),
                    rider.getLongitude(),
                    restaurant.getLat(),
                    restaurant.getLon()
            );
        }

        String status = order.getRiderDeliveryStage();

        return new RiderOrderView(
                order.getId(),
                restaurant != null
                        ? restaurant.getName()
                        : order.getRestaurantName(),
                status,
                order.getTotal(),
                items,
                order.getCreatedAt() != null
                        ? DATE_FORMAT.format(order.getCreatedAt())
                        : null,
                order.getPinCode(),
                distance,
                order.getRiderId(),
                order.getPaymentMethod(),
                null,
                order.getStatus() == OrderStatus.DELIVERED
                        && order.getStatusUpdatedAt() != null
                        ? DATE_FORMAT.format(order.getStatusUpdatedAt())
                        : null,
                restaurant != null ? restaurant.getArea() : null,
                order.getAddress(),
                customer != null ? customer.getName() : null,
                customer != null ? customer.getPhone() : null,
                restaurant != null ? restaurant.getLat() : null,
                restaurant != null ? restaurant.getLon() : null,
                order.getLat(),
                order.getLon()
        );
    }

    private double distanceKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {

        double earthRadius = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return earthRadius * 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );
    }
}
