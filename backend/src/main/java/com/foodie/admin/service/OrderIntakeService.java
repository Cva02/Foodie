package com.foodie.admin.service;

import com.foodie.admin.dto.DeliveryPoint;
import com.foodie.admin.dto.DeliveryTracking;
import com.foodie.admin.dto.OrderIntakeRequest;
import com.foodie.admin.dto.OrderIntakeResponse;
import com.foodie.admin.model.FoodOrder;
import com.foodie.admin.model.FoodOrderItem;
import com.foodie.admin.repository.DeliveryLocationRepository;
import com.foodie.admin.repository.FoodOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class OrderIntakeService {

    private final FoodOrderRepository orders;
    private final DeliveryLocationRepository locations;

    public OrderIntakeService(FoodOrderRepository orders, DeliveryLocationRepository locations) {
        this.orders = orders;
        this.locations = locations;
    }

    @Transactional
    public OrderIntakeResponse receiveOrder(OrderIntakeRequest request) {
        String orderId = request.orderId().trim();
        if (orders.existsById(orderId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An order with this ID already exists");
        }

        Instant orderedAt = Instant.now();
        Instant expectedDeliveryAt = request.expectedDeliveryAt() == null
                ? orderedAt.plus(40, ChronoUnit.MINUTES) : request.expectedDeliveryAt();
        if (!expectedDeliveryAt.isAfter(orderedAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected delivery time must be in the future");
        }

        String trackingToken = UUID.randomUUID().toString().replace("-", "");
        FoodOrder order = FoodOrder.readyOrder(orderId, request.customerName().trim(),
                request.customerPhone().trim(), request.sellerId().trim(), request.sellerName().trim(),
                request.sellerAddress().trim(), request.customerAddress().trim(), request.customerPinCode().trim(),
                request.customerLatitude(), request.customerLongitude(),
                request.sellerLatitude(), request.sellerLongitude(), request.amount(),
                request.payment().trim(), List.of(new FoodOrderItem(request.itemName().trim(),
                        request.amount() / request.quantity(), request.quantity())),
                orderedAt, expectedDeliveryAt, trackingToken, false);
        orders.save(order);
        return new OrderIntakeResponse(orderId, "READY", trackingToken);
    }

    @Transactional(readOnly = true)
    public DeliveryTracking trackOrder(String trackingToken) {
        FoodOrder order = orders.findByTrackingToken(trackingToken).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Tracking link not found"));
        List<DeliveryPoint> route = locations.findByOrderIdOrderByRecordedAtAsc(order.getId()).stream()
                .map(point -> new DeliveryPoint(point.getLatitude(), point.getLongitude(), point.getRecordedAt()))
                .toList();
        String pickupOtp = "RIDER_ASSIGNED".equals(order.getStatus()) ? order.getPickupOtp() : null;
        String deliveryOtp = "DISPATCHED".equals(order.getStatus()) ? order.getDeliveryOtp() : null;
        return new DeliveryTracking(order.getId(), order.getCustomerName(), order.getSellerName(),
                order.getSellerAddress(), order.getSellerLatitude(), order.getSellerLongitude(),
                order.getDeliveryAddress(), order.getCustomerPinCode(),
                order.getCustomerLatitude(), order.getCustomerLongitude(),
                order.getItemsSummary(), order.getStatus(),
                order.getRiderName(), order.getCreatedAt(), order.getExpectedDeliveryAt(),
                order.getDeliveredAt(), order.isDemoOrder(), pickupOtp, deliveryOtp, route);
    }
}
