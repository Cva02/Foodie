package com.foodie.admin.validation;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class DeliveryPinCodes {

    private DeliveryPinCodes() {
    }

    public static String normalize(String value) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Enter two different six-digit delivery PIN codes");
        }
        String[] pinCodes = value.trim().split("\\s*,\\s*");
        if (pinCodes.length != 2 || !pinCodes[0].matches("[0-9]{6}")
                || !pinCodes[1].matches("[0-9]{6}") || pinCodes[0].equals(pinCodes[1])) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Enter two different six-digit delivery PIN codes");
        }
        return pinCodes[0] + "," + pinCodes[1];
    }
}
