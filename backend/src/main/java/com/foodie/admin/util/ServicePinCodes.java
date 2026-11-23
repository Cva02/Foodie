package com.foodie.admin.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ServicePinCodes {

    private ServicePinCodes() {
    }

    public static String normalizePair(String value) {
        String[] pinCodes = value.trim().split("\\s*,\\s*");
        if (pinCodes.length != 2 || !pinCodes[0].matches("[0-9]{6}")
                || !pinCodes[1].matches("[0-9]{6}") || pinCodes[0].equals(pinCodes[1])) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Enter two different six-digit delivery PIN codes");
        }
        return pinCodes[0] + "," + pinCodes[1];
    }
}
