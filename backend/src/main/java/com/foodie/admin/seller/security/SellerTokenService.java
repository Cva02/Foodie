
package com.foodie.admin.seller.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Component
public class SellerTokenService {

    private final byte[] key;
    private final long ttlSeconds;

    public SellerTokenService(
            @Value("${foodie.auth.secret}") String secret,
            @Value("${foodie.auth.token-days}") long days) {

        this.key = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = days * 86400;
    }

    public String create(String sellerId) {
        String payload = sellerId + ":" +
                (Instant.now().getEpochSecond() + ttlSeconds);

        String encodedPayload = encode(
                payload.getBytes(StandardCharsets.UTF_8)
        );

        return encodedPayload + "." + encode(sign(encodedPayload));
    }

    public String verify(String token) {
        try {
            if (token == null || token.isBlank()) {
                return null;
            }

            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                return null;
            }

            byte[] expectedSignature = sign(parts[0]);
            byte[] actualSignature =
                    Base64.getUrlDecoder().decode(parts[1]);

            if (!MessageDigest.isEqual(
                    expectedSignature, actualSignature)) {
                return null;
            }

            String payload = new String(
                    Base64.getUrlDecoder().decode(parts[0]),
                    StandardCharsets.UTF_8
            );

            int separator = payload.lastIndexOf(':');
            if (separator <= 0) {
                return null;
            }

            String sellerId = payload.substring(0, separator);
            long expiry = Long.parseLong(
                    payload.substring(separator + 1)
            );

            if (sellerId.isBlank()
                    || expiry < Instant.now().getEpochSecond()) {
                return null;
            }

            return sellerId;

        } catch (Exception exception) {
            return null;
        }
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));

            return mac.doFinal(
                    value.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not sign Seller token", exception
            );
        }
    }

    private static String encode(byte[] value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value);
    }
}
