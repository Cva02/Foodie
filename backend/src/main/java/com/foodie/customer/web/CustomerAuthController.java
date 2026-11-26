package com.foodie.customer.web;

import com.foodie.customer.dto.Dtos.*;
import com.foodie.customer.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/auth")
@RequiredArgsConstructor
public class CustomerAuthController {
    private final AccountService accounts;

    @PostMapping("/signup")
    public AuthRes signup(@Valid @RequestBody SignupReq req) {
        return accounts.signup(req);
    }

    @PostMapping("/login")
    public AuthRes login(@Valid @RequestBody LoginReq req) {
        return accounts.login(req);
    }
}
