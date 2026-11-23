package com.foodie.admin.security;

import com.foodie.admin.model.PortalAccount;
import com.foodie.admin.model.Rider;
import com.foodie.admin.model.RiderPrivateDetails;
import com.foodie.admin.repository.PortalAccountRepository;
import com.foodie.admin.repository.RiderPrivateDetailsRepository;
import com.foodie.admin.repository.RiderRepository;
import com.foodie.admin.dto.RiderRegisterRequest;
import com.foodie.admin.util.ServicePinCodes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PortalAuthService {

    public record Identity(String profileId, String name, String email, String phone) {
    }

    private record Session(Identity identity, Instant expiresAt) {
    }

    private static final String RIDER_ROLE = "RIDER";
    private final PortalAccountRepository accounts;
    private final RiderRepository riders;
    private final RiderPrivateDetailsRepository privateDetails;
    private final PasswordEncoder encoder;
    private final RiderRegistrationFileValidator fileValidator;
    private final Duration lifetime;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public PortalAuthService(PortalAccountRepository accounts, RiderRepository riders,
                            RiderPrivateDetailsRepository privateDetails,
                            RiderRegistrationFileValidator fileValidator,
                            PasswordEncoder encoder,
                            @Value("${foodie.auth.token-hours:12}") long lifetimeHours) {
        this.accounts = accounts;
        this.riders = riders;
        this.privateDetails = privateDetails;
        this.fileValidator = fileValidator;
        this.encoder = encoder;
        this.lifetime = Duration.ofHours(lifetimeHours);
    }

    @Transactional
    public synchronized Map<String, String> registerRider(RiderRegisterRequest request,
                                                           MultipartFile vehiclePhoto,
                                                           MultipartFile licenseDocument) {
        RiderRegistrationFileValidator.Upload vehicleUpload = fileValidator.validateVehiclePhoto(vehiclePhoto);
        RiderRegistrationFileValidator.Upload licenseUpload = fileValidator.validateLicenseDocument(licenseDocument);
        String normalizedEmail = normalizeEmail(request.email());
        ensureEmailAvailable(normalizedEmail);
        if (riders.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A rider profile already uses this email");
        }

        String profileId = "R" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        String displayName = request.name().trim();
        String normalizedPhone = request.phone().trim();
        Rider rider = riders.save(new Rider(profileId, displayName, null, normalizedPhone, normalizedEmail,
                request.vehicle().trim(), request.vehicleNumber().trim().toUpperCase(Locale.ROOT),
                request.licenseNumber().trim().toUpperCase(Locale.ROOT), request.city().trim(),
                ServicePinCodes.normalizePair(request.servicePinCodes()), "OFFLINE", Rider.DEFAULT_RATING,
                0, 0.0, today()));
        privateDetails.save(new RiderPrivateDetails(profileId, request.bankName().trim(),
                request.accountHolderName().trim(), request.bankAccountNumber(),
                request.bankIfsc().trim().toUpperCase(Locale.ROOT), vehicleUpload.content(),
                vehicleUpload.contentType(), licenseUpload.content(), licenseUpload.contentType()));
        PortalAccount account = accounts.save(new PortalAccount(normalizedEmail, encoder.encode(request.password()),
                RIDER_ROLE, displayName, rider.getId(), normalizedPhone));
        return issue(account);
    }

    public Map<String, String> login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        PortalAccount account = accounts.findByEmailIgnoreCase(normalizedEmail).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!RIDER_ROLE.equals(account.getRole()) || !encoder.matches(password, account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        if (!"ACTIVE".equals(account.getStatus()) || !riders.existsById(account.getProfileId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rider account is not active");
        }
        return issue(account);
    }

    public Identity identity(String token) {
        if (token == null) return null;
        Session session = sessions.get(token);
        if (session == null) return null;
        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return null;
        }
        PortalAccount account = accounts.findByEmailIgnoreCase(session.identity().email()).orElse(null);
        if (account == null || !RIDER_ROLE.equals(account.getRole())
                || !"ACTIVE".equals(account.getStatus()) || !riders.existsById(account.getProfileId())) {
            sessions.remove(token);
            return null;
        }
        return session.identity();
    }

    public void logout(String token) {
        if (token != null) sessions.remove(token);
    }

    private Map<String, String> issue(PortalAccount account) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Identity identity = new Identity(account.getProfileId(), account.getDisplayName(),
                account.getEmail(), account.getPhone());
        sessions.put(token, new Session(identity, Instant.now().plus(lifetime)));
        return Map.of("token", token, "username", identity.name(), "role", RIDER_ROLE,
                "userId", identity.profileId(), "email", identity.email(),
                "phone", identity.phone() == null ? "" : identity.phone());
    }

    private void ensureEmailAvailable(String email) {
        if (accounts.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String today() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
    }
}
