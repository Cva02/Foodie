package com.foodie.admin.controller;

import com.foodie.admin.dto.DeliveryTracking;
import com.foodie.admin.service.OrderIntakeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tracking")
public class OrderTrackingController {

    private final OrderIntakeService orderIntake;

    public OrderTrackingController(OrderIntakeService orderIntake) {
        this.orderIntake = orderIntake;
    }

    @GetMapping("/{token}")
    public DeliveryTracking trackOrder(@PathVariable String token) {
        return orderIntake.trackOrder(token);
    }
}
