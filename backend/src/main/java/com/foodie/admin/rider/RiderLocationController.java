package com.foodie.admin.rider;

import com.foodie.admin.rider.dto.RiderLocationRequest;
import com.foodie.admin.rider.security.RiderAuthInterceptor;
import com.foodie.admin.rider.service.RiderLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rider/location")
@RequiredArgsConstructor
public class RiderLocationController {

    private final RiderLocationService riderLocationService;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLocation(
            @RequestAttribute(RiderAuthInterceptor.RIDER_ID_ATTR) String riderId,
            @RequestBody RiderLocationRequest request
    ) {
        riderLocationService.updateLocation(riderId, request);
    }
}