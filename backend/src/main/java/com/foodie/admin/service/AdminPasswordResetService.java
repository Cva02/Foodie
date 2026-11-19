package com.foodie.admin.service;

import com.foodie.admin.model.Admin;
import com.foodie.admin.repository.AdminRepository;
import com.foodie.admin.security.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminPasswordResetService {

    private record ResetChallenge(String username, String mobile, Instant expiresAt) {
    }

    private static final String REQUEST_MESSAGE =
            "If the admin account exists, an OTP has been sent or may be resent after a short wait.";

    private final AdminRepository admins;
    private final PasswordEncoder passwordEncoder;
    private final Msg91OtpService otp;
    private final AuthService auth;
    private final String configuredMobile;
    private final int otpExpiryMinutes;
    private final Map<String, ResetChallenge> challenges = new ConcurrentHashMap<>();

    public AdminPasswordResetService(AdminRepository admins, PasswordEncoder passwordEncoder,
                                     Msg91OtpService otp, AuthService auth,
                                     @Value("${foodie.admin.phone:}") String configuredMobile,
                                     @Value("${foodie.msg91.otp-expiry-minutes:5}") int otpExpiryMinutes) {
        this.admins = admins;
        this.passwordEncoder = passwordEncoder;
        this.otp = otp;
        this.auth = auth;
        this.configuredMobile = configuredMobile;
        this.otpExpiryMinutes = otpExpiryMinutes;
    }

    public Map<String, String> requestOtp(String username) {
        otp.validateConfiguration();
        String mobile = normalizeMobile(configuredMobile);
        String normalizedUsername = username == null ? "" : username.trim();
        String requestId = UUID.randomUUID().toString();
        Admin admin = admins.findByUsername(normalizedUsername).orElse(null);
        if (admin != null) {
            try {
                otp.send(mobile);
            } catch (ResponseStatusException ex) {
                if (!HttpStatus.TOO_MANY_REQUESTS.equals(ex.getStatusCode())) throw ex;
            }
            Instant now = Instant.now();
            challenges.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
            challenges.put(requestId, new ResetChallenge(admin.getUsername(), mobile,
                    now.plusSeconds(Math.max(1, otpExpiryMinutes) * 60L)));
        }
        return Map.of("message", REQUEST_MESSAGE, "requestId", requestId);
    }

    @Transactional
    public synchronized Map<String, String> resetPassword(String requestId, String username,
                                                          String code, String newPassword) {
        String normalizedUsername = username == null ? "" : username.trim();
        ResetChallenge challenge = challenges.get(requestId);
        if (challenge == null || challenge.expiresAt().isBefore(Instant.now())
                || !challenge.username().equalsIgnoreCase(normalizedUsername)) {
            challenges.remove(requestId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }
        Admin admin = admins.findByUsername(challenge.username()).orElse(null);
        if (admin == null || !otp.verify(challenge.mobile(), code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }
        admin.setPasswordHash(passwordEncoder.encode(newPassword));
        admins.save(admin);
        challenges.remove(requestId);
        auth.revokeSessionsFor(admin.getUsername());
        return Map.of("message", "Password updated. Sign in with your new password.");
    }

    private static String normalizeMobile(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        if (digits.length() == 10) return "91" + digits;
        if (digits.length() == 11 && digits.startsWith("0")) return "91" + digits.substring(1);
        if (digits.length() >= 11 && digits.length() <= 15) return digits;
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Admin SMS recovery is not configured. Set FOODIE_ADMIN_PHONE in backend/.env or the backend environment.");
    }
}
