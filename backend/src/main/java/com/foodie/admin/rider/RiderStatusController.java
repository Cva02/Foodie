package com.foodie.admin.rider;

import com.foodie.admin.rider.dto.RiderProfileDtos.RiderProfileView;
import com.foodie.admin.rider.security.RiderAuthInterceptor;
import com.foodie.admin.rider.service.RiderProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rider/status")
@RequiredArgsConstructor
public class RiderStatusController {

    private final RiderProfileService riderProfileService;

    @PatchMapping
    public RiderProfileView updateStatus(
            @RequestAttribute(RiderAuthInterceptor.RIDER_ID_ATTR) String riderId,
            @RequestParam String value
    ) {
        return riderProfileService.updateStatus(riderId, value);
    }
}