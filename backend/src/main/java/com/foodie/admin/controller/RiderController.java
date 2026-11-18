package com.foodie.admin.controller;

import com.foodie.admin.model.Detail;
import com.foodie.admin.model.Rider;
import com.foodie.admin.model.RiderProfileUpdate;
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
@RequestMapping("/api/riders")
public class RiderController {

    private final AdminService service;

    public RiderController(AdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rider> all() {
        return service.riders();
    }

    @GetMapping("/{id}")
    public Detail<Rider> one(@PathVariable String id) {
        return service.rider(id);
    }

    @PatchMapping("/{id}/status")
    public Rider setStatus(@PathVariable String id, @RequestParam("value") String value) {
        return service.setRiderStatus(id, value);
    }

    @PatchMapping("/{id}/profile")
    public Rider updateProfile(@PathVariable String id, @RequestBody RiderProfileUpdate profile) {
        return service.updateRiderProfile(id, profile);
    }
}
