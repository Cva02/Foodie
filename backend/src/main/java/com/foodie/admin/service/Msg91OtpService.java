package com.foodie.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriBuilder;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Msg91OtpService {

    private static final long SEND_COOLDOWN_SECONDS = 60;

    private final RestClient client;
    private final String authKey;
    private final String templateId;
    private final int otpLength;
    private final int otpExpiryMinutes;
    private final Map<String, Instant> lastSent = new ConcurrentHashMap<>();

    public Msg91OtpService(RestClient.Builder builder,
                           @Value("${foodie.msg91.base-url}") String baseUrl,
                           @Value("${foodie.msg91.auth-key:}") String authKey,
                           @Value("${foodie.msg91.template-id:}") String templateId,
                           @Value("${foodie.msg91.otp-length:6}") int otpLength,
                           @Value("${foodie.msg91.otp-expiry-minutes:5}") int otpExpiryMinutes) {
        this.client = builder.baseUrl(baseUrl).build();
        this.authKey = authKey;
        this.templateId = templateId;
        this.otpLength = otpLength;
        this.otpExpiryMinutes = otpExpiryMinutes;
    }

    public synchronized void send(String mobile) {
        requireConfiguration();
        Instant now = Instant.now();
        Instant previous = lastSent.get(mobile);
        if (previous != null && previous.plusSeconds(SEND_COOLDOWN_SECONDS).isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Please wait before requesting another OTP");
        }

        JsonNode response;
        try {
            response = client.post()
                    .uri(uri -> otpUri(uri, mobile).build())
                    .header("authkey", authKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Map.of())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException ex) {
            throw providerUnavailable(ex);
        }
        if (!isSuccess(response)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "MSG91 could not send the OTP");
        }
        lastSent.put(mobile, now);
        lastSent.entrySet().removeIf(entry -> entry.getValue().plusSeconds(SEND_COOLDOWN_SECONDS).isBefore(now));
    }

    public boolean verify(String mobile, String otp) {
        requireConfiguration();
        JsonNode response;
        try {
            response = client.get()
                    .uri(uri -> uri.path("/api/v5/otp/verify")
                            .queryParam("mobile", mobile)
                            .queryParam("otp", otp)
                            .build())
                    .header("authkey", authKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException ex) {
            throw providerUnavailable(ex);
        }
        return isSuccess(response);
    }

    public void validateConfiguration() {
        requireConfiguration();
    }

    private UriBuilder otpUri(UriBuilder uri, String mobile) {
        return uri.path("/api/v5/otp")
                .queryParam("template_id", templateId)
                .queryParam("mobile", mobile)
                .queryParam("otp_length", otpLength)
                .queryParam("otp_expiry", otpExpiryMinutes);
    }

    private void requireConfiguration() {
        if (authKey.isBlank() || templateId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Password reset SMS is not configured. Set MSG91_AUTH_KEY and MSG91_TEMPLATE_ID in backend/.env or the backend environment.");
        }
        if (otpLength < 4 || otpLength > 8 || otpExpiryMinutes < 1 || otpExpiryMinutes > 60) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "MSG91 OTP length or expiry configuration is invalid");
        }
    }

    private static boolean isSuccess(JsonNode response) {
        return response != null && "success".equalsIgnoreCase(response.path("type").asText());
    }

    private static ResponseStatusException providerUnavailable(RestClientException cause) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not contact the MSG91 OTP service", cause);
    }
}
