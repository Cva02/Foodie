package com.foodie.admin.rider;

import com.foodie.admin.rider.security.RiderAuthInterceptor;
import com.foodie.admin.rider.service.RiderNotificationService;
import com.foodie.admin.rider.service.RiderNotificationService.RiderNotificationView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rider/notifications")
@RequiredArgsConstructor
public class RiderNotificationController {

    private final RiderNotificationService riderNotificationService;

    @GetMapping
    public List<RiderNotificationView> getNotifications(
            @RequestAttribute(RiderAuthInterceptor.RIDER_ID_ATTR) String riderId
    ) {
        return riderNotificationService.getNotifications(riderId);
    }

    @PatchMapping("/{id}/read")
    public RiderNotificationView markAsRead(
            @RequestAttribute(RiderAuthInterceptor.RIDER_ID_ATTR) String riderId,
            @PathVariable Long id
    ) {
        return riderNotificationService.markAsRead(riderId, id);
    }
}