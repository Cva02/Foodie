package com.foodie.admin.controller;

import com.foodie.admin.dto.LoginRequest;
import com.foodie.admin.dto.AdminPasswordResetConfirmRequest;
import com.foodie.admin.dto.AdminPasswordResetRequest;
import com.foodie.admin.security.AuthInterceptor;
import com.foodie.admin.security.AuthService;
import com.foodie.admin.service.AdminPasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final AdminPasswordResetService passwordResets;

    public AuthController(AuthService auth, AdminPasswordResetService passwordResets) {
        this.auth = auth;
        this.passwordResets = passwordResets;
    }

    /** Public. Returns {token, username}. */
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest body) {
        return auth.login(body.username(), body.password());
    }

    @PostMapping("/password-reset/request")
    public Map<String, String> requestPasswordReset(@Valid @RequestBody AdminPasswordResetRequest body) {
        return passwordResets.requestOtp(body.username());
    }

    @PostMapping("/password-reset/confirm")
    public Map<String, String> confirmPasswordReset(@Valid @RequestBody AdminPasswordResetConfirmRequest body) {
        return passwordResets.resetPassword(body.requestId(), body.username(), body.otp(), body.newPassword());
    }

    /** Who am I? (also a cheap way to check the stored token is still valid) */
    @GetMapping("/me")
    public Map<String, String> me(HttpServletRequest req) {
        return Map.of("username", (String) req.getAttribute(AuthInterceptor.USER_ATTR));
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest req) {
        auth.logout(AuthInterceptor.tokenOf(req));
        return Map.of("message", "Logged out");
    }
}
