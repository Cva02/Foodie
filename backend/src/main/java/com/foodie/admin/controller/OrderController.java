package com.foodie.admin.controller;

import com.foodie.admin.model.Order;
import com.foodie.admin.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Admin live-order feed (all orders, newest first). */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final AdminService service;

    public OrderController(AdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<Order> all() {
        return service.orders();
    }
}
