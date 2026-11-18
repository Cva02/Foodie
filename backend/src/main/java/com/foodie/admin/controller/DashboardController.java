package com.foodie.admin.controller;

import com.foodie.admin.model.DashboardStats;
import com.foodie.admin.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AdminService service;

    public DashboardController(AdminService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardStats stats() {
        return service.dashboard();
    }
}
