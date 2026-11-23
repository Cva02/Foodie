package com.foodie.admin.service;

import com.foodie.admin.model.FoodOrder;
import com.foodie.admin.model.FoodOrderItem;
import com.foodie.admin.repository.DeliveryLocationRepository;
import com.foodie.admin.repository.FoodOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderIntakeServiceTest {

    @Mock
    private FoodOrderRepository orders;
    @Mock
    private DeliveryLocationRepository locations;
    @InjectMocks
    private OrderIntakeService service;

    @Test
    void trackingRevealsOnlyTheOtpForTheNextCheckpoint() {
        Instant orderedAt = Instant.parse("2026-01-01T10:00:00Z");
        FoodOrder order = FoodOrder.readyOrder("ORDER-OTP", "Test Customer", "+919876543210",
                "REST-01", "Test Restaurant", "12 Restaurant Road", "4 Customer Street", "531162",
                12.1, 77.1, 12.0, 77.0, 180.0, "UPI",
                List.of(new FoodOrderItem("Dosa", 180.0, 1)), orderedAt,
                orderedAt.plusSeconds(2400), "tracking-token", false);
        when(orders.findByTrackingToken("tracking-token")).thenReturn(Optional.of(order));
        when(locations.findByOrderIdOrderByRecordedAtAsc("ORDER-OTP")).thenReturn(List.of());

        assertNull(service.trackOrder("tracking-token").pickupOtp());
        assertNull(service.trackOrder("tracking-token").deliveryOtp());

        order.assignRider("R1", "Test Rider");
        var pickupStage = service.trackOrder("tracking-token");
        assertEquals(order.getPickupOtp(), pickupStage.pickupOtp());
        assertNull(pickupStage.deliveryOtp());

        order.setStatus("DISPATCHED");
        var deliveryStage = service.trackOrder("tracking-token");
        assertNull(deliveryStage.pickupOtp());
        assertEquals(order.getDeliveryOtp(), deliveryStage.deliveryOtp());
    }
}
