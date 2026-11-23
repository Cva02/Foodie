package com.foodie.admin.data;

import com.foodie.admin.model.FoodOrder;
import com.foodie.admin.model.Rider;
import com.foodie.admin.repository.FoodOrderRepository;
import com.foodie.admin.repository.RiderRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class DataSeederTest {

    @Test
    void seedsDemoOrdersAcrossTheSupportedBheemunipatnamPinCodes() {
        FoodOrderRepository orders = Mockito.mock(FoodOrderRepository.class);
        RiderRepository riders = Mockito.mock(RiderRepository.class);
        when(orders.findAll()).thenReturn(List.of());
        when(orders.findById(anyString())).thenReturn(java.util.Optional.empty());
        when(orders.save(any(FoodOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        new DataSeeder(orders, riders).run();

        ArgumentCaptor<FoodOrder> captor = ArgumentCaptor.forClass(FoodOrder.class);
        verify(orders, times(23)).save(captor.capture());
        List<FoodOrder> seededOrders = captor.getAllValues();
        Set<String> seededIds = seededOrders.stream().map(FoodOrder::getId).collect(Collectors.toSet());
        assertTrue(seededIds.containsAll(Set.of("DEMO-1011", "DEMO-1012", "DEMO-1013", "DEMO-1014",
                "DEMO-1015", "DEMO-1016", "DEMO-1017", "DEMO-1018", "DEMO-1019", "DEMO-1020",
                "DEMO-1021", "DEMO-1022", "DEMO-1023")));
        assertTrue(seededOrders.stream().allMatch(order -> "READY".equals(order.getStatus())
                && order.getTrackingToken() != null
                && order.getSellerAddress().contains("Bheemunipatnam")
                && order.getDeliveryAddress().contains("Bheemunipatnam")
                && Set.of("531162", "531163", "530048", "530045").contains(order.getCustomerPinCode())
                && order.getPickupOtp().matches("\\d{6}")
                && order.getDeliveryOtp().matches("\\d{6}")));
        assertEquals(23, seededIds.size());
    }

    @Test
    void existingRidersWithoutServicePinCodesAreAssignedTheDefaultBheemunipatnamArea() {
        FoodOrderRepository orders = Mockito.mock(FoodOrderRepository.class);
        RiderRepository riders = Mockito.mock(RiderRepository.class);
        Rider rider = new Rider("R1", "Test Rider", null, "+919876543210", "rider@example.com",
                "Bike", "KA01AB1234", "DL1234567890", "Bheemunipatnam", null,
                "OFFLINE", 5.0, 0, 0.0, "Today");
        when(orders.findAll()).thenReturn(List.of());
        when(orders.findById(anyString())).thenReturn(java.util.Optional.empty());
        when(orders.save(any(FoodOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(riders.findAll()).thenReturn(List.of(rider));

        new DataSeeder(orders, riders).run();

        assertEquals("531162,531163", rider.getServicePinCodes());
        verify(riders).saveAll(List.of(rider));
    }

    @Test
    void existingUnratedRidersReceiveTheDefaultFiveStarRating() {
        FoodOrderRepository orders = Mockito.mock(FoodOrderRepository.class);
        RiderRepository riders = Mockito.mock(RiderRepository.class);
        Rider rider = new Rider("R1", "Test Rider", null, "+919876543210", "rider@example.com",
                "Bike", "KA01AB1234", "DL1234567890", "Bheemunipatnam", "531162,531163",
                "OFFLINE", 0.0, 0, 0.0, "Today");
        when(orders.findAll()).thenReturn(List.of());
        when(orders.findById(anyString())).thenReturn(java.util.Optional.empty());
        when(orders.save(any(FoodOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(riders.findAll()).thenReturn(List.of(rider));

        new DataSeeder(orders, riders).run();

        assertEquals(5.0, rider.getRating());
        verify(riders).saveAll(List.of(rider));
    }
}
