package com.foodie.customer.web;

import com.foodie.customer.dto.Dtos.*;
import com.foodie.customer.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accounts;

    @GetMapping
    public UserDto me(@RequestAttribute("uid") Long uid) {
        return accounts.me(uid);
    }

    @PutMapping
    public UserDto update(@RequestAttribute("uid") Long uid, @Valid @RequestBody ProfileReq req) {
        return accounts.updateProfile(uid, req);
    }

    @PutMapping("/password")
    public void password(@RequestAttribute("uid") Long uid, @Valid @RequestBody PasswordReq req) {
        accounts.changePassword(uid, req);
    }

    /** "Use my current location" / manual location: stores the delivery area on the profile. */
    @PutMapping("/location")
    public UserDto location(@RequestAttribute("uid") Long uid, @Valid @RequestBody LocationReq req) {
        return accounts.setLocation(uid, req);
    }

    @GetMapping("/addresses")
    public List<AddressDto> addresses(@RequestAttribute("uid") Long uid) {
        return accounts.addresses(uid);
    }

    @PostMapping("/addresses")
    public AddressDto addAddress(@RequestAttribute("uid") Long uid, @Valid @RequestBody AddressReq req) {
        return accounts.addAddress(uid, req);
    }

    @DeleteMapping("/addresses/{id}")
    public void deleteAddress(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        accounts.deleteAddress(uid, id);
    }
}
