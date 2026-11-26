package com.foodie.admin.seller.controller;

import com.foodie.admin.seller.dto.SellerOrderDtos.OrderView;
import com.foodie.admin.seller.dto.SellerOrderDtos.StatusRequest;
import com.foodie.admin.seller.security.SellerAuthInterceptor;
import com.foodie.admin.seller.service.SellerOrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/orders")
public class SellerOrderController {

    private final SellerOrderService orders;

    public SellerOrderController(SellerOrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    public List<OrderView> list(
            @RequestParam(defaultValue = "all") String group,
            HttpServletRequest request) {
        String sellerId = (String) request.getAttribute(
                SellerAuthInterceptor.SELLER_ID_ATTR);
        return orders.listOrders(sellerId, group);
    }

    @PutMapping("/{orderId}/status")
    public OrderView update(
            @PathVariable Long orderId,
            @RequestBody StatusRequest body,
            HttpServletRequest request) {
        String sellerId = (String) request.getAttribute(
                SellerAuthInterceptor.SELLER_ID_ATTR);
        return orders.updateStatus(sellerId, orderId, body);
    }
}
