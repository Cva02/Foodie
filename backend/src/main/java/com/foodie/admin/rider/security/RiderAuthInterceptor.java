package com.foodie.admin.rider.security;

import com.foodie.admin.model.PortalAccount;
import com.foodie.admin.model.Rider;
import com.foodie.admin.repository.PortalAccountRepository;
import com.foodie.admin.repository.RiderRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RiderAuthInterceptor implements HandlerInterceptor {

    public static final String RIDER_ID_ATTR = "riderId";

    private final RiderTokenService tokens;
    private final PortalAccountRepository accounts;
    private final RiderRepository riders;

    public RiderAuthInterceptor(
            RiderTokenService tokens,
            PortalAccountRepository accounts,
            RiderRepository riders) {

        this.tokens = tokens;
        this.accounts = accounts;
        this.riders = riders;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return reject(response, HttpStatus.UNAUTHORIZED,
                    "Please log in to continue", null);
        }

        String riderId = tokens.verify(header.substring(7));

        if (riderId == null) {
            return reject(response, HttpStatus.UNAUTHORIZED,
                    "Your session is invalid or expired", null);
        }

        Rider rider = riders.findById(riderId).orElse(null);

        if (rider == null) {
            return reject(response, HttpStatus.UNAUTHORIZED,
                    "Rider account not found", null);
        }

        PortalAccount account = accounts.findByProfileId(riderId)
                .orElse(null);

        if (account == null
                || !"RIDER".equalsIgnoreCase(account.getRole())
                || !"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            return reject(response, HttpStatus.FORBIDDEN,
                    "Your Rider account is not active", null);
        }

        if ("SUSPENDED".equalsIgnoreCase(rider.getStatus())) {
            return reject(response, HttpStatus.FORBIDDEN,
                    "Your Rider account has been suspended",
                    rider.getStatusNote());
        }

        if (!"OFFLINE".equalsIgnoreCase(rider.getStatus())
                && !"ONLINE".equalsIgnoreCase(rider.getStatus())
                && !"ON_DELIVERY".equalsIgnoreCase(rider.getStatus())) {
            return reject(response, HttpStatus.FORBIDDEN,
                    "Your Rider profile is not approved or active", null);
        }

        request.setAttribute(RIDER_ID_ATTR, riderId);
        return true;
    }

    private boolean reject(
            HttpServletResponse response,
            HttpStatus status,
            String message,
            String reason) throws IOException {

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);

        if (reason != null && !reason.isBlank()) {
            body.put("reason", reason);
        }

        response.getWriter().write(
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(body)
        );

        return false;
    }
}