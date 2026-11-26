package com.foodie.admin.rider.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rider/auth")
public class RiderAuthController {

    private final RiderAuthService authService;

    public RiderAuthController(RiderAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody RiderLoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request.email(), request.password())
        );
    }
}