package com.foodie.admin.service;

import com.foodie.admin.model.FoodOrder;
import com.foodie.admin.model.FoodOrderItem;
import com.foodie.admin.model.Order;
import com.foodie.admin.model.Rider;
import com.foodie.admin.repository.DeliveryLocationRepository;
import com.foodie.admin.repository.FoodOrderRepository;
import com.foodie.admin.repository.PortalAccountRepository;
import com.foodie.admin.repository.RiderDeclinedOrderRepository;
import com.foodie.admin.repository.RiderNotificationRepository;
import com.foodie.admin.repository.RiderRepository;
import com.foodie.admin.repository.RiderSupportTicketRepository;
import com.foodie.admin.model.RiderNotification;
import com.foodie.admin.model.RiderSupportTicket;
import com.foodie.admin.dto.RiderSupportRequest;
import com.foodie.admin.dto.RiderProfileUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PortalServiceTest {

    @Mock
    private RiderRepository riders;
    @Mock
    private FoodOrderRepository orders;
    @Mock
    private PortalAccountRepository accounts;
    @Mock
    private RiderDeclinedOrderRepository declinedOrders;
    @Mock
    private DeliveryLocationRepository locations;
    @Mock
    private RiderSupportTicketRepository supportTickets;
    @Mock
    private RiderNotificationRepository notifications;
    @InjectMocks
    private PortalService service;

    @Test
    void availableOrdersAreSortedByDistanceAndHideCustomerAddress() {
        Rider rider = rider("R1", "ONLINE");
        rider.updateLocation(12.0, 77.0);
        FoodOrder farOrder = order("FAR", 12.2);
        FoodOrder nearOrder = order("NEAR", 12.001);
        FoodOrder outOfAreaOrder = order("OUTSIDE", 12.002, "531164");
        when(riders.findById("R1")).thenReturn(Optional.of(rider));
        when(declinedOrders.findByRiderId("R1")).thenReturn(List.of());
        when(orders.findByStatusAndRiderIdIsNullOrderByCreatedAtAsc("READY"))
                .thenReturn(List.of(farOrder, nearOrder, outOfAreaOrder));
        when(orders.findByRiderIdOrderByCreatedAtDesc("R1")).thenReturn(List.of());

        List<Order> result = service.riderOrders("R1");

        assertEquals(List.of("NEAR", "FAR"), result.stream().map(Order::id).toList());
        assertNotNull(result.get(0).distanceKm());
        assertEquals("", result.get(0).customerName());
        assertNull(result.get(0).customerPhone());
        assertEquals("", result.get(0).address());
        assertEquals("531162", result.get(0).customerPinCode());
        assertNull(result.get(0).customerLatitude());
        assertNull(result.get(0).customerLongitude());
        assertEquals("12 Restaurant Road", result.get(0).sellerAddress());
    }

    @Test
    void supportRequestsAreSavedForTheAuthenticatedRider() {
        when(riders.findById("R1")).thenReturn(Optional.of(rider("R1", "ONLINE")));
        when(supportTickets.save(any(RiderSupportTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createSupportTicket("R1",
                new RiderSupportRequest("  Payment issue  ", "  Please check my payout.  "));

        assertEquals("Payment issue", result.subject());
        assertEquals("Please check my payout.", result.message());
        assertEquals("OPEN", result.status());
        verify(supportTickets).save(argThat(ticket -> "R1".equals(ticket.getRiderId())
                && "Payment issue".equals(ticket.getSubject())
                && "Please check my payout.".equals(ticket.getMessage())));
    }

    @Test
    void updatesEditableRiderProfileAndNormalizesDeliveryPinCodes() {
        Rider rider = rider("R1", "OFFLINE");
        when(riders.findLockedById("R1")).thenReturn(Optional.of(rider));
        when(riders.save(any(Rider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rider updated = service.updateRiderProfile("R1", new RiderProfileUpdateRequest(
                " New Rider ", "+919876543210", "Bike", "ka01ab1234", "dl1234567890",
                " Bheemunipatnam ", "531162, 531163"));

        assertEquals("New Rider", updated.getName());
        assertEquals("Bheemunipatnam", updated.getCity());
        assertEquals("KA01AB1234", updated.getVehicleNumber());
        assertEquals("DL1234567890", updated.getLicenseNumber());
        assertEquals("531162,531163", updated.getServicePinCodes());
    }

    @Test
    void rejectsDuplicateDeliveryPinCodesWhenUpdatingProfile() {
        Rider rider = rider("R1", "OFFLINE");
        when(riders.findLockedById("R1")).thenReturn(Optional.of(rider));

        assertThrows(ResponseStatusException.class, () -> service.updateRiderProfile("R1",
                new RiderProfileUpdateRequest("Rider", "+919876543210", "Bike", "KA01AB1234",
                        "DL1234567890", "Bheemunipatnam", "531162,531162")));

        verify(riders, never()).save(any(Rider.class));
        assertEquals("531162,531163", rider.getServicePinCodes());
    }

    @Test
    void assignedOrdersIncludePickupAndDeliveryCoordinatesForTheRouteMap() {
        Rider rider = rider("R1", "ON_DELIVERY");
        FoodOrder assignedOrder = order("ORDER-3", 12.0);
        assignedOrder.assignRider("R1", "Test Rider");
        when(riders.findById("R1")).thenReturn(Optional.of(rider));
        when(declinedOrders.findByRiderId("R1")).thenReturn(List.of());
        when(orders.findByRiderIdOrderByCreatedAtDesc("R1")).thenReturn(List.of(assignedOrder));

        List<Order> result = service.riderOrders("R1");

        assertEquals(12.0, result.get(0).sellerLatitude());
        assertEquals(77.0, result.get(0).sellerLongitude());
        assertEquals(12.1, result.get(0).customerLatitude());
        assertEquals(77.1, result.get(0).customerLongitude());
    }

    @Test
    void decliningHidesAnOrderOnlyForThatRider() {
        FoodOrder order = order("ORDER-1", 12.0);
        Rider rider = rider("R1", "ONLINE");
        when(orders.findLockedById("ORDER-1")).thenReturn(Optional.of(order));
        when(riders.findLockedById("R1")).thenReturn(Optional.of(rider));
        when(declinedOrders.existsByRiderIdAndOrderId("R1", "ORDER-1")).thenReturn(false);

        service.declineRide("ORDER-1", "R1");

        verify(declinedOrders).save(argThat(declinedFor("R1", "ORDER-1")));
        verify(orders, never()).save(order);
        assertEquals("READY", order.getStatus());
        assertNull(order.getRiderId());
    }

    @Test
    void acceptingAnOrderCreatesANotificationForTheAcceptingRider() {
        FoodOrder order = order("ORDER-ACCEPTED", 12.0);
        Rider rider = rider("R1", "ONLINE");
        when(orders.findLockedById("ORDER-ACCEPTED")).thenReturn(Optional.of(order));
        when(orders.save(any(FoodOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(riders.findLockedById("R1")).thenReturn(Optional.of(rider));
        when(declinedOrders.existsByRiderIdAndOrderId("R1", "ORDER-ACCEPTED")).thenReturn(false);

        service.acceptRide("ORDER-ACCEPTED", "R1");

        verify(notifications).save(argThat(notification -> "R1".equals(notification.getRiderId())
                && "ORDER-ACCEPTED".equals(notification.getOrderId())
                && RiderNotification.ORDER_ACCEPTED.equals(notification.getType())));
    }

    @Test
    void notificationInboxReturnsOnlyUnreadNotifications() {
        RiderNotification unread = new RiderNotification("R1", "ORDER-1",
                RiderNotification.ORDER_ACCEPTED, "Order ORDER-1 accepted.");
        when(riders.findById("R1")).thenReturn(Optional.of(rider("R1", "ONLINE")));
        when(notifications.findTop50ByRiderIdAndReadAtIsNullOrderByCreatedAtDesc("R1"))
                .thenReturn(List.of(unread));

        var result = service.riderNotifications("R1");

        assertEquals(1, result.size());
        assertEquals(RiderNotification.ORDER_ACCEPTED, result.get(0).type());
        verify(notifications).findTop50ByRiderIdAndReadAtIsNullOrderByCreatedAtDesc("R1");
    }

    @Test
    void riderCannotAcceptAnOrderOutsideTheirServicePinCodes() {
        FoodOrder order = order("ORDER-OUTSIDE", 12.0, "531164");
        Rider rider = rider("R1", "ONLINE");
        when(orders.findLockedById("ORDER-OUTSIDE")).thenReturn(Optional.of(order));
        when(riders.findLockedById("R1")).thenReturn(Optional.of(rider));
        when(declinedOrders.existsByRiderIdAndOrderId("R1", "ORDER-OUTSIDE")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.acceptRide("ORDER-OUTSIDE", "R1"));

        assertEquals("READY", order.getStatus());
        assertEquals("ONLINE", rider.getStatus());
        verify(orders, never()).save(order);
        verify(riders, never()).save(rider);
    }

    @Test
    void onDeliveryLocationUpdatesAreRecordedForCustomerTracking() {
        Rider rider = rider("R1", "ON_DELIVERY");
        FoodOrder order = order("ORDER-2", 12.0);
        order.assignRider("R1", "Test Rider");
        when(riders.findLockedById("R1")).thenReturn(Optional.of(rider));
        when(orders.findByRiderIdAndStatusIn("R1", List.of("RIDER_ASSIGNED", "PICKED_UP", "DISPATCHED")))
                .thenReturn(List.of(order));

        service.updateLocation("R1", 12.123, 77.456);

        verify(locations).save(argThat(location -> "ORDER-2".equals(location.getOrderId())
                && "R1".equals(location.getRiderId())
                && location.getLatitude() == 12.123
                && location.getLongitude() == 77.456));
        assertEquals(12.123, rider.getLatitude());
        assertEquals(77.456, rider.getLongitude());
    }

    @Test
    void pickupAndDeliveryRequireTheirOrderOtps() {
        FoodOrder order = order("ORDER-OTP", 12.0);
        order.assignRider("R1", "Test Rider");
        Rider rider = rider("R1", "ON_DELIVERY");
        when(orders.findLockedById("ORDER-OTP")).thenReturn(Optional.of(order));
        when(orders.save(any(FoodOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(riders.findLockedById("R1")).thenReturn(Optional.of(rider));

        assertThrows(ResponseStatusException.class,
                () -> service.updateRide("ORDER-OTP", "R1", "PICKED_UP", wrongOtp(order.getPickupOtp())));
        assertEquals("RIDER_ASSIGNED", order.getStatus());

        service.updateRide("ORDER-OTP", "R1", "PICKED_UP", order.getPickupOtp());
        service.updateRide("ORDER-OTP", "R1", "DISPATCHED", null);
        assertThrows(ResponseStatusException.class,
                () -> service.updateRide("ORDER-OTP", "R1", "DELIVERED", wrongOtp(order.getDeliveryOtp())));
        assertEquals("DISPATCHED", order.getStatus());

        service.updateRide("ORDER-OTP", "R1", "DELIVERED", order.getDeliveryOtp());

        assertEquals("DELIVERED", order.getStatus());
        verify(riders).save(rider);
        verify(notifications).save(argThat(notification -> "R1".equals(notification.getRiderId())
                && "ORDER-OTP".equals(notification.getOrderId())
                && RiderNotification.ORDER_DELIVERED.equals(notification.getType())));
    }

    private static ArgumentMatcher<com.foodie.admin.model.RiderDeclinedOrder> declinedFor(
            String riderId, String orderId) {
        return declined -> riderId.equals(declined.getRiderId()) && orderId.equals(declined.getOrderId());
    }

    private static Rider rider(String id, String status) {
        return new Rider(id, "Test Rider", null, "+919876543210", "rider@example.com",
                "Bike", "KA01AB1234", "DL1234567890", "Bheemunipatnam", "531162,531163",
                status, 5.0, 0, 0.0, "Today");
    }

    private static FoodOrder order(String id, double sellerLatitude) {
        return order(id, sellerLatitude, "531162");
    }

    private static FoodOrder order(String id, double sellerLatitude, String customerPinCode) {
        Instant orderedAt = Instant.parse("2026-01-01T10:00:00Z");
        return FoodOrder.readyOrder(id, "Test Customer", "+919876543211", "REST-01",
                "Test Restaurant", "12 Restaurant Road", "4 Customer Street", customerPinCode, 12.1, 77.1,
                sellerLatitude, 77.0, 180.0, "UPI",
                List.of(new FoodOrderItem("Dosa", 180.0, 1)), orderedAt,
                orderedAt.plusSeconds(2400), "tracking-" + id, false);
    }

    private static String wrongOtp(String otp) {
        return "000000".equals(otp) ? "000001" : "000000";
    }
}
