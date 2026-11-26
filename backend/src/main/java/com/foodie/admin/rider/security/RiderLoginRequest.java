package com.foodie.admin.rider.security;

public record RiderLoginRequest(
        String email,
        String password
) {}