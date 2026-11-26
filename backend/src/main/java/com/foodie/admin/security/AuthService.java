package com.foodie.admin.security;

import com.foodie.admin.model.Admin;
import com.foodie.admin.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Authenticates the admin console and manages its sessions. */
@Service
public class AuthService {

    private record Session(String username, Instant expiresAt) {
    }

    private final AdminRepository admins;
    private final PasswordEncoder encoder;
    private final Duration lifetime;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthService(AdminRepository admins, PasswordEncoder encoder,
                       @Value("${foodie.auth.token-hours:12}") long hours) {
        this.admins = admins;
        this.encoder = encoder;
        this.lifetime = Duration.ofHours(hours);
    }

    public Map<String, String> login(String username, String password) {
        Admin admin = admins.findByUsername(username == null ? "" : username.trim()).orElse(null);
        // Same message for "no such user" and "wrong password".
        if (admin == null || !encoder.matches(password, admin.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, new Session(admin.getUsername(), Instant.now().plus(lifetime)));
        return Map.of("token", token, "username", admin.getUsername());
    }

    /** @return the username the token belongs to, or null if it is unknown / expired. */
    public String usernameFor(String token) {
        if (token == null) return null;
        Session s = sessions.get(token);
        if (s == null) return null;
        if (s.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return null;
        }
        return s.username();
    }

    public void logout(String token) {
        if (token != null) sessions.remove(token);
    }

    public void revokeSessionsFor(String username) {
        sessions.entrySet().removeIf(entry ->
                entry.getValue().username().equalsIgnoreCase(username));
    }
}
