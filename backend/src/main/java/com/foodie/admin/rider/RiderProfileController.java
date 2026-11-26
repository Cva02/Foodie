package com.foodie.admin.rider;

import com.foodie.admin.rider.dto.RiderProfileDtos.RiderProfileView;
import com.foodie.admin.rider.dto.RiderProfileDtos.UpdateRiderProfileRequest;
import com.foodie.admin.rider.security.RiderAuthInterceptor;
import com.foodie.admin.rider.service.RiderProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rider/profile")
@RequiredArgsConstructor
public class RiderProfileController {

    private final RiderProfileService riderProfileService;

    @GetMapping
    public RiderProfileView getProfile(
            @RequestAttribute(RiderAuthInterceptor.RIDER_ID_ATTR) String riderId
    ) {
        return riderProfileService.getProfile(riderId);
    }

    @PatchMapping
    public RiderProfileView updateProfile(
            @RequestAttribute(RiderAuthInterceptor.RIDER_ID_ATTR) String riderId,
            @RequestBody UpdateRiderProfileRequest request
    ) {
        return riderProfileService.updateProfile(riderId, request);
    }
}