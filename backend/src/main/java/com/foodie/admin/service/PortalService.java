package com.foodie.admin.service;

import com.foodie.admin.dto.RiderSupportRequest;
import com.foodie.admin.dto.RiderSupportTicketResponse;
import com.foodie.admin.dto.RiderNotificationResponse;
import com.foodie.admin.dto.RiderProfileUpdateRequest;
import com.foodie.admin.model.FoodOrder;
import com.foodie.admin.model.DeliveryLocation;
import com.foodie.admin.model.Order;
import com.foodie.admin.model.PortalAccount;
import com.foodie.admin.model.Rider;
import com.foodie.admin.model.RiderDeclinedOrder;
import com.foodie.admin.model.RiderNotification;
import com.foodie.admin.model.RiderSupportTicket;
import com.foodie.admin.repository.DeliveryLocationRepository;
import com.foodie.admin.repository.FoodOrderRepository;
import com.foodie.admin.repository.PortalAccountRepository;
import com.foodie.admin.repository.RiderDeclinedOrderRepository;
import com.foodie.admin.repository.RiderNotificationRepository;
import com.foodie.admin.repository.RiderRepository;
import com.foodie.admin.repository.RiderSupportTicketRepository;
import com.foodie.admin.util.ServicePinCodes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PortalService {

    private static final double RIDER_EARNING_PER_DELIVERY = 40.0;
    private final RiderRepository riders;
    private final FoodOrderRepository orders;
    private final PortalAccountRepository accounts;
    private final RiderDeclinedOrderRepository declinedOrders;
    private final DeliveryLocationRepository locations;
    private final RiderSupportTicketRepository supportTickets;
    private final RiderNotificationRepository notifications;

    public PortalService(RiderRepository riders, FoodOrderRepository orders, PortalAccountRepository accounts,
                         RiderDeclinedOrderRepository declinedOrders, DeliveryLocationRepository locations,
                         RiderSupportTicketRepository supportTickets, RiderNotificationRepository notifications) {
        this.riders = riders;
        this.orders = orders;
        this.accounts = accounts;
        this.declinedOrders = declinedOrders;
        this.locations = locations;
        this.supportTickets = supportTickets;
        this.notifications = notifications;
    }

    public Rider riderProfile(String riderId) {
        return requireRider(riderId);
    }

    @Transactional
    public Rider updateRiderProfile(String riderId, RiderProfileUpdateRequest request) {
        Rider rider = requireLockedRider(riderId);
        String servicePinCodes = ServicePinCodes.normalizePair(request.servicePinCodes());
        rider.updateProfile(request.name().trim(), request.phone().trim(), request.vehicle(),
                request.vehicleNumber().trim().toUpperCase(java.util.Locale.ROOT),
                request.licenseNumber().trim().toUpperCase(java.util.Locale.ROOT),
                request.city().trim(), servicePinCodes);
        return riders.save(rider);
    }

    @Transactional
    public RiderSupportTicketResponse createSupportTicket(String riderId, RiderSupportRequest request) {
        requireRider(riderId);
        RiderSupportTicket ticket = supportTickets.save(
                new RiderSupportTicket(riderId, request.subject().trim(), request.message().trim()));
        return supportTicketResponse(ticket);
    }

    public List<RiderSupportTicketResponse> riderSupportTickets(String riderId) {
        requireRider(riderId);
        return supportTickets.findByRiderIdOrderByCreatedAtDesc(riderId).stream()
                .map(PortalService::supportTicketResponse)
                .toList();
    }

    public List<RiderNotificationResponse> riderNotifications(String riderId) {
        requireRider(riderId);
        return notifications.findTop50ByRiderIdAndReadAtIsNullOrderByCreatedAtDesc(riderId).stream()
                .map(PortalService::notificationResponse)
                .toList();
    }

    @Transactional
    public RiderNotificationResponse markNotificationRead(Long notificationId, String riderId) {
        RiderNotification notification = notifications.findByIdAndRiderId(notificationId, riderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.markRead();
        return notificationResponse(notifications.save(notification));
    }

    @Transactional
    public Rider setRiderAvailability(String riderId, String status) {
        if (!"ONLINE".equals(status) && !"OFFLINE".equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose ONLINE or OFFLINE");
        }
        Rider rider = requireLockedRider(riderId);
        if ("ON_DELIVERY".equals(rider.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Finish the current delivery before going offline");
        }
        rider.setStatus(status);
        return riders.save(rider);
    }

    public List<Order> riderOrders(String riderId) {
        Map<String, FoodOrder> combined = new LinkedHashMap<>();
        Rider rider = requireRider(riderId);
        Set<String> declinedIds = declinedOrders.findByRiderId(riderId).stream()
                .map(declined -> declined.getOrderId())
                .collect(Collectors.toSet());
        if ("ONLINE".equals(rider.getStatus())) {
            orders.findByStatusAndRiderIdIsNullOrderByCreatedAtAsc("READY")
                    .stream()
                    .filter(order -> !declinedIds.contains(order.getId()))
                    .filter(order -> rider.servesPinCode(order.getCustomerPinCode()))
                    .forEach(order -> combined.put(order.getId(), order));
        }
        orders.findByRiderIdOrderByCreatedAtDesc(riderId)
                .forEach(order -> combined.put(order.getId(), order));
        return combined.values().stream()
                .map(order -> riderOrder(order, distanceFromRider(rider, order)))
                .sorted(Comparator.comparing(Order::distanceKm,
                        Comparator.nullsLast(Comparator.<Double>naturalOrder())))
                .toList();
    }

    @Transactional
    public Order acceptRide(String orderId, String riderId) {
        FoodOrder order = requireLockedOrder(orderId);
        requireStatus(order, "READY");
        if (order.getRiderId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This delivery has already been accepted");
        }
        if (declinedOrders.existsByRiderIdAndOrderId(riderId, orderId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You declined this delivery");
        }
        Rider rider = requireLockedRider(riderId);
        if (!"ONLINE".equals(rider.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Rider is not available for a delivery");
        }
        if (!rider.servesPinCode(order.getCustomerPinCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is outside your delivery PIN codes");
        }
        rider.setStatus("ON_DELIVERY");
        riders.save(rider);
        order.assignRider(rider.getId(), rider.getName());
        FoodOrder acceptedOrder = orders.save(order);
        notifications.save(new RiderNotification(rider.getId(), acceptedOrder.getId(),
                RiderNotification.ORDER_ACCEPTED, "Order " + acceptedOrder.getId() + " accepted."));
        return acceptedOrder.toOrderWithoutCustomerPhone();
    }

    @Transactional
    public void declineRide(String orderId, String riderId) {
        FoodOrder order = requireLockedOrder(orderId);
        requireStatus(order, "READY");
        if (order.getRiderId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This delivery has already been accepted");
        }
        Rider rider = requireLockedRider(riderId);
        if (!"ONLINE".equals(rider.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Rider must be online to decline a delivery");
        }
        if (!declinedOrders.existsByRiderIdAndOrderId(riderId, orderId)) {
            declinedOrders.save(new RiderDeclinedOrder(riderId, orderId));
        }
    }

    @Transactional
    public void updateLocation(String riderId, double latitude, double longitude) {
        Rider rider = requireLockedRider(riderId);
        if (!"ONLINE".equals(rider.getStatus()) && !"ON_DELIVERY".equals(rider.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Rider must be online to share location");
        }
        rider.updateLocation(latitude, longitude);
        riders.save(rider);

        if ("ON_DELIVERY".equals(rider.getStatus())) {
            List<FoodOrder> activeOrders = orders.findByRiderIdAndStatusIn(riderId,
                    List.of("RIDER_ASSIGNED", "PICKED_UP", "DISPATCHED"));
            if (activeOrders.size() != 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not identify the active delivery");
            }
            locations.save(new DeliveryLocation(activeOrders.get(0).getId(), riderId,
                    latitude, longitude, java.time.Instant.now()));
        }
    }

    @Transactional
    public Order updateRide(String orderId, String riderId, String status, String otp) {
        FoodOrder order = requireLockedOrder(orderId);
        if (!riderId.equals(order.getRiderId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order does not belong to your account");
        }
        Map<String, String> transitions = Map.of(
                "RIDER_ASSIGNED", "PICKED_UP",
                "PICKED_UP", "DISPATCHED",
                "DISPATCHED", "DELIVERED");
        if (!status.equals(transitions.get(order.getStatus()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid order status transition");
        }
        if ("PICKED_UP".equals(status) && !Objects.equals(order.getPickupOtp(), otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pickup OTP");
        }
        if ("DELIVERED".equals(status) && !Objects.equals(order.getDeliveryOtp(), otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid delivery OTP");
        }

        order.setStatus(status);
        if ("DELIVERED".equals(status)) {
            Rider rider = requireLockedRider(riderId);
            rider.setStatus("ONLINE");
            rider.completeDelivery(RIDER_EARNING_PER_DELIVERY);
            riders.save(rider);
        }
        FoodOrder updatedOrder = orders.save(order);
        if ("DELIVERED".equals(status)) {
            notifications.save(new RiderNotification(riderId, updatedOrder.getId(),
                    RiderNotification.ORDER_DELIVERED, "Order " + updatedOrder.getId() + " delivered."));
        }
        return updatedOrder.toOrderWithoutCustomerPhone();
    }

    private Order riderOrder(FoodOrder order, Double distanceKm) {
        if (order.getRiderId() == null) return order.toAvailableOrder(distanceKm);
        Order view = order.toOrder(distanceKm);
        if (view.customerPhone() != null && !view.customerPhone().isBlank()) return view;
        String customerPhone = accounts.findByProfileId(order.getCustomerId())
                .map(PortalAccount::getPhone).orElse(null);
        return view.withCustomerPhone(customerPhone);
    }

    private static RiderSupportTicketResponse supportTicketResponse(RiderSupportTicket ticket) {
        return new RiderSupportTicketResponse(ticket.getId(), ticket.getSubject(), ticket.getMessage(),
                ticket.getStatus(), ticket.getCreatedAt());
    }

    private static RiderNotificationResponse notificationResponse(RiderNotification notification) {
        return new RiderNotificationResponse(notification.getId(), notification.getOrderId(),
                notification.getType(), notification.getMessage(), notification.getCreatedAt(),
                notification.getReadAt());
    }

    private static Double distanceFromRider(Rider rider, FoodOrder order) {
        if (rider.getLatitude() == null || rider.getLongitude() == null
                || order.getSellerLatitude() == null || order.getSellerLongitude() == null) {
            return null;
        }
        double latitudeDistance = Math.toRadians(order.getSellerLatitude() - rider.getLatitude());
        double longitudeDistance = Math.toRadians(order.getSellerLongitude() - rider.getLongitude());
        double startLatitude = Math.toRadians(rider.getLatitude());
        double endLatitude = Math.toRadians(order.getSellerLatitude());
        double haversine = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                + Math.cos(startLatitude) * Math.cos(endLatitude)
                * Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private Rider requireRider(String riderId) {
        return riders.findById(riderId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Rider not found"));
    }

    private Rider requireLockedRider(String riderId) {
        return riders.findLockedById(riderId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Rider not found"));
    }

    private FoodOrder requireLockedOrder(String orderId) {
        return orders.findLockedById(orderId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private void requireStatus(FoodOrder order, String status) {
        if (!status.equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is no longer in the expected stage");
        }
    }
}
