
package com.foodie.admin.seller.controller;

import com.foodie.admin.seller.dto.SellerProfileDtos.AvailabilityRequest;
import com.foodie.admin.seller.dto.SellerProfileDtos.ProfileView;
import com.foodie.admin.seller.security.SellerAuthInterceptor;
import com.foodie.admin.seller.service.SellerProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/profile")
public class SellerProfileController {

    private final SellerProfileService profileService;

    public SellerProfileController(SellerProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileView getProfile(HttpServletRequest request) {
        String sellerId = (String) request.getAttribute(
                SellerAuthInterceptor.SELLER_ID_ATTR);

        return profileService.getProfile(sellerId);
    }

    @PatchMapping("/availability")
    public ProfileView updateAvailability(
            HttpServletRequest request,
            @RequestBody AvailabilityRequest body) {
        String sellerId = (String) request.getAttribute(
                SellerAuthInterceptor.SELLER_ID_ATTR);

        return profileService.updateAvailability(sellerId, body.open());
    }
}
