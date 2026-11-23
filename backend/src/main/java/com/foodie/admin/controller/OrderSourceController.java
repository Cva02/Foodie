package com.foodie.admin.controller;

import com.foodie.admin.dto.OrderIntakeRequest;
import com.foodie.admin.dto.OrderIntakeResponse;
import com.foodie.admin.service.OrderIntakeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/order-source")
public class OrderSourceController {

    private final OrderIntakeService orderIntake;
    private final String sourceApiKey;

    public OrderSourceController(OrderIntakeService orderIntake,
                                 @Value("${foodie.order-source.api-key:}") String sourceApiKey) {
        this.orderIntake = orderIntake;
        this.sourceApiKey = sourceApiKey;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderIntakeResponse receiveOrder(
            @RequestHeader(name = "X-Order-Source-Key", required = false) String providedKey,
            @Valid @RequestBody OrderIntakeRequest request) {
        if (sourceApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Order intake is not configured");
        }
        if (providedKey == null || !MessageDigest.isEqual(
                sourceApiKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid order source key");
        }
        return orderIntake.receiveOrder(request);
    }
}
