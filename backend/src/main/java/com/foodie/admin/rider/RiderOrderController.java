
package com.foodie.admin.rider;

import com.foodie.admin.rider.security.RiderAuthInterceptor;
import com.foodie.admin.rider.service.RiderOrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rider")
@RequiredArgsConstructor
public class RiderOrderController {

    private final RiderOrderService riderOrderService;

    @GetMapping("/orders")
    public Object getOrders(
            @RequestAttribute(RiderAuthInterceptor.RIDER_ID_ATTR)
            String riderId
    ) {
        return riderOrderService.getOrders(riderId);
    }

    @PatchMapping("/orders/{orderId}/{action}")
    public Map<String, String> updateOrder(
            @RequestAttribute(RiderAuthInterceptor.RIDER_ID_ATTR)
            String riderId,
            @PathVariable Long orderId,
            @PathVariable String action,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String otp = body == null ? null : body.get("otp");

        String message;

        if ("accept".equals(action)) {
            message = riderOrderService.acceptOrder(riderId, orderId);
        } else {
            message = riderOrderService.performAction(
                    riderId, orderId, action, otp
            );
        }

        return Map.of("message", message);
    }
}
