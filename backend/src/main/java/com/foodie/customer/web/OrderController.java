package com.foodie.customer.web;

import com.foodie.customer.dto.Dtos.*;
import com.foodie.customer.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orders;

    @PostMapping
    public OrderDto place(@RequestAttribute("uid") Long uid, @Valid @RequestBody PlaceOrderReq req) {
        return orders.place(uid, req);
    }

    @PostMapping("/coupons/validate")
    public CouponQuote validateCoupon(@RequestAttribute("uid") Long uid, @Valid @RequestBody CouponReq req) {
        return orders.validateCoupon(uid, req.couponCode(), req.restaurantId(), req.items());
    }

    @GetMapping
    public List<OrderDto> list(@RequestAttribute("uid") Long uid) {
        return orders.list(uid);
    }

    @GetMapping("/{id}")
    public OrderDto get(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        return orders.get(uid, id);
    }

    @PostMapping("/{id}/cancel")
    public OrderDto cancel(@RequestAttribute("uid") Long uid, @PathVariable Long id,
                           @RequestBody(required = false) CancelReq req) {
        return orders.cancel(uid, id, req == null ? null : req.reason());
    }
}
