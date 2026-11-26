
package com.foodie.admin.seller.security;

import com.foodie.admin.model.PortalAccount;
import com.foodie.admin.model.Seller;
import com.foodie.admin.repository.PortalAccountRepository;
import com.foodie.admin.repository.SellerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class SellerAuthService {

    private final PortalAccountRepository accounts;
    private final SellerRepository sellers;
    private final PasswordEncoder passwordEncoder;
    private final SellerTokenService tokens;

    public SellerAuthService(
            PortalAccountRepository accounts,
            SellerRepository sellers,
            PasswordEncoder passwordEncoder,
            SellerTokenService tokens) {

        this.accounts = accounts;
        this.sellers = sellers;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    public Map<String, Object> login(String email, String password) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        PortalAccount account = accounts
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!"SELLER".equalsIgnoreCase(account.getRole())
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
                    "Your Seller account is not active"
            );
        }

        if (account.getProfileId() == null
                || account.getProfileId().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your Seller account is not linked to a restaurant"
            );
        }

        Seller seller = sellers.findById(account.getProfileId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Your restaurant account could not be found"
                ));

        if ("SUSPENDED".equalsIgnoreCase(seller.getStatus())) {
            String reason = seller.getStatusNote();

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    reason == null || reason.isBlank()
                            ? "Your restaurant account has been suspended"
                            : "Your restaurant account has been suspended: " + reason
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(seller.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your restaurant is not approved yet"
            );
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("token", tokens.create(seller.getId()));
        response.put("sellerId", seller.getId());
        response.put("restaurantName", seller.getName());
        response.put("ownerName", seller.getOwnerName());
        response.put("role", "SELLER");
        response.put("status", seller.getStatus());

        return response;
    }
}
