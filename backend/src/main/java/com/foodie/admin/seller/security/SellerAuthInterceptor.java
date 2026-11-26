
package com.foodie.admin.seller.security;

import com.foodie.admin.model.PortalAccount;
import com.foodie.admin.model.Seller;
import com.foodie.admin.repository.PortalAccountRepository;
import com.foodie.admin.repository.SellerRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

@Component
public class SellerAuthInterceptor implements HandlerInterceptor {

    public static final String SELLER_ID_ATTR = "sellerId";

    private final SellerTokenService tokens;
    private final PortalAccountRepository accounts;
    private final SellerRepository sellers;

    public SellerAuthInterceptor(
            SellerTokenService tokens,
            PortalAccountRepository accounts,
            SellerRepository sellers) {

        this.tokens = tokens;
        this.accounts = accounts;
        this.sellers = sellers;
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
            return reject(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to continue",
                    null
            );
        }

        String sellerId = tokens.verify(header.substring(7));

        if (sellerId == null) {
            return reject(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Your session is invalid or expired",
                    null
            );
        }

        Seller seller = sellers.findById(sellerId).orElse(null);

        if (seller == null) {
            return reject(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Seller account not found",
                    null
            );
        }

        PortalAccount account = accounts.findByProfileId(sellerId)
                .orElse(null);

        if (account == null
                || !"SELLER".equalsIgnoreCase(account.getRole())
                || !"ACTIVE".equalsIgnoreCase(account.getStatus())) {

            return reject(
                    response,
                    HttpStatus.FORBIDDEN,
                    "Your Seller account is not active",
                    null
            );
        }

        if ("SUSPENDED".equalsIgnoreCase(seller.getStatus())) {
            return reject(
                    response,
                    HttpStatus.FORBIDDEN,
                    "Your restaurant account has been suspended",
                    seller.getStatusNote()
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(seller.getStatus())) {
            return reject(
                    response,
                    HttpStatus.FORBIDDEN,
                    "Your restaurant is not approved yet",
                    null
            );
        }

        request.setAttribute(SELLER_ID_ATTR, sellerId);
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

        Map<String, Object> body = new java.util.LinkedHashMap<>();
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
