package com.foodie.admin.controller;

import com.foodie.admin.dto.OtpVerificationRequest;
import com.foodie.admin.dto.RiderLoginRequest;
import com.foodie.admin.dto.RiderLocationRequest;
import com.foodie.admin.dto.RiderProfileUpdateRequest;
import com.foodie.admin.dto.RiderRegisterRequest;
import com.foodie.admin.dto.RiderSupportRequest;
import com.foodie.admin.dto.RiderSupportTicketResponse;
import com.foodie.admin.dto.RiderNotificationResponse;
import com.foodie.admin.model.Order;
import com.foodie.admin.model.Rider;
import com.foodie.admin.security.AuthInterceptor;
import com.foodie.admin.security.PortalAuthService;
import com.foodie.admin.service.PortalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rider")
public class PortalController {

    private final PortalAuthService auth;
    private final PortalService riderService;

    public PortalController(PortalAuthService auth, PortalService riderService) {
        this.auth = auth;
        this.riderService = riderService;
    }

    @PostMapping(value = "/auth/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(
            @Valid @RequestPart("rider") RiderRegisterRequest body,
            @RequestPart("vehiclePhoto") MultipartFile vehiclePhoto,
            @RequestPart("licenseDocument") MultipartFile licenseDocument) {
        return auth.registerRider(body, vehiclePhoto, licenseDocument);
    }

    @PostMapping("/auth/login")
    public Map<String, String> login(@Valid @RequestBody RiderLoginRequest body) {
        return auth.login(body.email(), body.password());
    }

    @PostMapping("/auth/logout")
    public Map<String, String> logout(HttpServletRequest request) {
        auth.logout(AuthInterceptor.tokenOf(request));
        return Map.of("message", "Logged out");
    }

    @GetMapping("/profile")
    public Rider profile(HttpServletRequest request) {
        return riderService.riderProfile(identity(request).profileId());
    }

    @PatchMapping("/profile")
    public Rider updateProfile(@Valid @RequestBody RiderProfileUpdateRequest body, HttpServletRequest request) {
        return riderService.updateRiderProfile(identity(request).profileId(), body);
    }

    @PatchMapping("/status")
    public Rider setAvailability(@RequestParam("value") String value, HttpServletRequest request) {
        return riderService.setRiderAvailability(identity(request).profileId(), value);
    }

    @GetMapping("/orders")
    public List<Order> orders(HttpServletRequest request) {
        return riderService.riderOrders(identity(request).profileId());
    }

    @GetMapping("/support")
    public List<RiderSupportTicketResponse> supportTickets(HttpServletRequest request) {
        return riderService.riderSupportTickets(identity(request).profileId());
    }

    @GetMapping("/notifications")
    public List<RiderNotificationResponse> notifications(HttpServletRequest request) {
        return riderService.riderNotifications(identity(request).profileId());
    }

    @PatchMapping("/notifications/{id}/read")
    public RiderNotificationResponse markNotificationRead(@PathVariable Long id, HttpServletRequest request) {
        return riderService.markNotificationRead(id, identity(request).profileId());
    }

    @PostMapping("/support")
    @ResponseStatus(HttpStatus.CREATED)
    public RiderSupportTicketResponse createSupportTicket(@Valid @RequestBody RiderSupportRequest body,
                                                           HttpServletRequest request) {
        return riderService.createSupportTicket(identity(request).profileId(), body);
    }

    @PostMapping("/location")
    public Map<String, String> updateLocation(@Valid @RequestBody RiderLocationRequest body,
                                             HttpServletRequest request) {
        riderService.updateLocation(identity(request).profileId(), body.latitude(), body.longitude());
        return Map.of("message", "Location updated");
    }

    @PatchMapping("/orders/{id}/accept")
    public Order acceptOrder(@PathVariable String id, HttpServletRequest request) {
        return riderService.acceptRide(id, identity(request).profileId());
    }

    @PatchMapping("/orders/{id}/decline")
    public Map<String, String> declineOrder(@PathVariable String id, HttpServletRequest request) {
        riderService.declineRide(id, identity(request).profileId());
        return Map.of("message", "Delivery dismissed for this rider");
    }

    @PatchMapping("/orders/{id}/picked-up")
    public Order markPickedUp(@PathVariable String id, @Valid @RequestBody OtpVerificationRequest body,
                              HttpServletRequest request) {
        return riderService.updateRide(id, identity(request).profileId(), "PICKED_UP", body.otp());
    }

    @PatchMapping("/orders/{id}/dispatched")
    public Order markDispatched(@PathVariable String id, HttpServletRequest request) {
        return riderService.updateRide(id, identity(request).profileId(), "DISPATCHED", null);
    }

    @PatchMapping("/orders/{id}/delivered")
    public Order markDelivered(@PathVariable String id, @Valid @RequestBody OtpVerificationRequest body,
                               HttpServletRequest request) {
        return riderService.updateRide(id, identity(request).profileId(), "DELIVERED", body.otp());
    }

    private static PortalAuthService.Identity identity(HttpServletRequest request) {
        Object value = request.getAttribute(AuthInterceptor.PORTAL_IDENTITY_ATTR);
        if (value instanceof PortalAuthService.Identity identity) return identity;
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in to continue");
    }
}
