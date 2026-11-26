package com.foodie.admin.controller;

import com.foodie.admin.model.Customer;
import com.foodie.admin.model.Detail;
import com.foodie.admin.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final AdminService service;

    public CustomerController(AdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<Customer> all() {
        return service.customers();
    }

    @GetMapping("/{id}")
    public Detail<Customer> one(@PathVariable String id) {
        return service.customer(id);
    }

    @PatchMapping("/{id}/status")
    public Customer setStatus(@PathVariable String id, @RequestParam("value") String value) {
        return service.setCustomerStatus(id, value);
    }
}
