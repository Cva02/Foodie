package com.foodie.customer.service;

import com.foodie.customer.config.TokenService;
import com.foodie.customer.dto.Dtos.*;
import com.foodie.customer.model.Address;
import com.foodie.customer.model.User;
import com.foodie.customer.repo.Repos.AddressRepo;
import com.foodie.customer.repo.Repos.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final UserRepo users;
    private final AddressRepo addresses;
    private final TokenService tokens;
    private final NotificationService notifications;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Transactional
    public AuthRes signup(SignupReq r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        User u = new User();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPhone(r.phone().trim());
        u.setPasswordHash(encoder.encode(r.password()));
        u = users.save(u);
        notifications.toUser(u.getId(), "WELCOME", "Welcome to Foodie!",
                "Your account is ready. Pick a restaurant and place your first order.", null);
        return new AuthRes(tokens.create(u.getId()), dto(u));
    }

    @Transactional(readOnly = true)
    public AuthRes login(LoginReq r) {
        User u = users.findByEmailIgnoreCase(r.email().trim())
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect email or password"));
        return new AuthRes(tokens.create(u.getId()), dto(u));
    }

    @Transactional(readOnly = true)
    public UserDto me(Long id) {
        return dto(find(id));
    }

    @Transactional
    public UserDto updateProfile(Long id, ProfileReq r) {
        User u = find(id);
        u.setName(r.name().trim());
        u.setPhone(r.phone() == null ? null : r.phone().trim());
        return dto(u);
    }

    @Transactional
    public void changePassword(Long id, PasswordReq r) {
        User u = find(id);
        if (!encoder.matches(r.currentPassword(), u.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        u.setPasswordHash(encoder.encode(r.newPassword()));
    }

    @Transactional
    public UserDto setLocation(Long id, LocationReq r) {
        User u = find(id);
        u.setLat(r.lat());
        u.setLon(r.lon());
        u.setLocationLabel(r.label());
        return dto(u);
    }

    @Transactional(readOnly = true)
    public List<AddressDto> addresses(Long uid) {
        return addresses.findByUserIdOrderByIdDesc(uid).stream().map(this::dto).toList();
    }

    @Transactional
    public AddressDto addAddress(Long uid, AddressReq r) {
        Address a = new Address();
        a.setUserId(uid);
        a.setLabel(r.label() == null || r.label().isBlank() ? "Other" : r.label());
        a.setLine(r.line().trim());
        a.setLat(r.lat());
        a.setLon(r.lon());
        return dto(addresses.save(a));
    }

    @Transactional
    public void deleteAddress(Long uid, Long id) {
        addresses.findById(id).filter(a -> a.getUserId().equals(uid)).ifPresent(addresses::delete);
    }

    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
    }

    private UserDto dto(User u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getLat(), u.getLon(), u.getLocationLabel());
    }

    private AddressDto dto(Address a) {
        return new AddressDto(a.getId(), a.getLabel(), a.getLine(), a.getLat(), a.getLon());
    }
}
