package com.foodie.customer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/** Minimal HMAC-SHA256 signed token: base64url("userId:expiryEpochSec") + "." + base64url(signature). */
@Component
public class TokenService {
    private final byte[] key;
    private final long ttlSeconds;

    public TokenService(@Value("${foodie.auth.secret}") String secret,
                        @Value("${foodie.auth.token-days}") long days) {
        this.key = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = days * 86400;
    }

    public String create(long userId) {
        String payload = userId + ":" + (Instant.now().getEpochSecond() + ttlSeconds);
        String p = b64(payload.getBytes(StandardCharsets.UTF_8));
        return p + "." + b64(hmac(p));
    }

    /** @return the user id, or null if the token is malformed, tampered with or expired. */
    public Long verify(String token) {
        try {
            if (token == null) return null;
            String[] parts = token.split("\\.");
            if (parts.length != 2) return null;
            byte[] expected = hmac(parts[0]);
            byte[] given = Base64.getUrlDecoder().decode(parts[1]);
            if (!MessageDigest.isEqual(expected, given)) return null;
            String[] payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8).split(":");
            if (Long.parseLong(payload[1]) < Instant.now().getEpochSecond()) return null;
            return Long.parseLong(payload[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String b64(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
