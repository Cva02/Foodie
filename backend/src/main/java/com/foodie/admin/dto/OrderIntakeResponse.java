package com.foodie.admin.dto;

public record OrderIntakeResponse(String orderId, String status, String trackingToken) {
}
