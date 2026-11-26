package com.foodie.admin.rider;

import com.foodie.admin.rider.dto.RiderSupportDtos.RiderSupportRequest;
import com.foodie.admin.rider.dto.RiderSupportDtos.RiderSupportView;
import com.foodie.admin.rider.security.RiderAuthInterceptor;
import com.foodie.admin.rider.service.RiderSupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rider/support")
@RequiredArgsConstructor
public class RiderSupportController {

    private final RiderSupportService riderSupportService;

    @GetMapping
    public List<RiderSupportView> getTickets(
            @RequestAttribute(RiderAuthInterceptor.RIDER_ID_ATTR) String riderId
    ) {
        return riderSupportService.getTickets(riderId);
    }

    @PostMapping
    public RiderSupportView createTicket(
            @RequestAttribute(RiderAuthInterceptor.RIDER_ID_ATTR) String riderId,
            @RequestBody RiderSupportRequest request
    ) {
        return riderSupportService.createTicket(riderId, request);
    }
}