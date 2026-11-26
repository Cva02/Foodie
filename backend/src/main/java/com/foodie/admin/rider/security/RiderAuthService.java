package com.foodie.admin.rider.security;

import com.foodie.admin.model.PortalAccount;
import com.foodie.admin.model.Rider;
import com.foodie.admin.repository.PortalAccountRepository;
import com.foodie.admin.repository.RiderRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class RiderAuthService {

    private final PortalAccountRepository accounts;
    private final RiderRepository riders;
    private final PasswordEncoder passwordEncoder;
    private final RiderTokenService tokens;

    public RiderAuthService(
            PortalAccountRepository accounts,
            RiderRepository riders,
            PasswordEncoder passwordEncoder,
            RiderTokenService tokens) {

        this.accounts = accounts;
        this.riders = riders;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    public Map<String, Object> login(String email, String password) {

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email and password are required"
            );
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        PortalAccount account = accounts
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!"RIDER".equalsIgnoreCase(account.getRole())
                || !passwordEncoder.matches(
                        password, account.getPasswordHash())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your Rider account is not active"
            );
        }

        if (account.getProfileId() == null
                || account.getProfileId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your Rider account is not linked to a rider profile"
            );
        }

        Rider rider = riders.findById(account.getProfileId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Your Rider profile could not be found"
                ));

        if ("SUSPENDED".equalsIgnoreCase(rider.getStatus())) {
            String reason = rider.getStatusNote();

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    reason == null || reason.isBlank()
                            ? "Your Rider account has been suspended"
                            : "Your Rider account has been suspended: " + reason
            );
        }

        if (!"OFFLINE".equalsIgnoreCase(rider.getStatus())
                && !"ONLINE".equalsIgnoreCase(rider.getStatus())
                && !"ON_DELIVERY".equalsIgnoreCase(rider.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your Rider profile is not approved or active"
            );
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("token", tokens.create(rider.getId()));
        response.put("riderId", rider.getId());
        response.put("name", rider.getName());
        response.put("phone", rider.getPhone());
        response.put("city", rider.getCity());
        response.put("vehicle", rider.getVehicle());
        response.put("vehicleNumber", rider.getVehicleNumber());
        response.put("avatar", rider.getAvatar());
        response.put("role", "RIDER");
        response.put("status", rider.getStatus());

        return response;
    }
}