package com.foodie.admin.controller;

import com.foodie.admin.model.Detail;
import com.foodie.admin.model.Seller;
import com.foodie.admin.model.SellerProfileUpdate;
import com.foodie.admin.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final AdminService service;

    public SellerController(AdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<Seller> all() {
        return service.sellers();
    }

    @GetMapping("/{id}")
    public Detail<Seller> one(@PathVariable String id) {
        return service.seller(id);
    }

    @PatchMapping("/{id}/status")
    public Seller setStatus(@PathVariable String id, @RequestParam("value") String value) {
        return service.setSellerStatus(id, value);
    }

    @PatchMapping("/{id}/profile")
    public Seller updateProfile(@PathVariable String id, @RequestBody SellerProfileUpdate profile) {
        return service.updateSellerProfile(id, profile);
    }
}
