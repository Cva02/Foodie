
package com.foodie.admin.seller.security;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/seller/auth")
public class SellerAuthController {

    private final SellerAuthService authService;

    public SellerAuthController(SellerAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(
            @Valid @RequestBody SellerLoginRequest request) {

        return authService.login(
                request.email(),
                request.password()
        );
    }
}
