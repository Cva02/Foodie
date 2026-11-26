
package com.foodie.admin.rider.service;

import com.foodie.admin.model.Rider;
import com.foodie.admin.repository.RiderRepository;
import com.foodie.admin.rider.dto.RiderProfileDtos.RiderProfileView;
import com.foodie.admin.rider.dto.RiderProfileDtos.UpdateRiderProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RiderProfileService {

    private final RiderRepository riderRepository;

    @Transactional(readOnly = true)
    public RiderProfileView getProfile(String riderId) {
        return toView(getRider(riderId));
    }

    @Transactional
    public RiderProfileView updateProfile(
            String riderId,
            UpdateRiderProfileRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Profile details are required."
            );
        }

        String name = clean(request.name());
        String phone = clean(request.phone());
        String vehicle = clean(request.vehicle());
        String vehicleNumber = clean(request.vehicleNumber());
        String licenseNumber = clean(request.licenseNumber());
        String city = clean(request.city());
        String servicePinCodes = clean(request.servicePinCodes());

        if (name == null || phone == null || vehicle == null
                || vehicleNumber == null || licenseNumber == null
                || city == null || servicePinCodes == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please complete all profile fields."
            );
        }

        if (!vehicle.equals("Bike") && !vehicle.equals("Scooty")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Choose Bike or Scooty as your vehicle."
            );
        }

        if (!servicePinCodes.matches("\\d{6}\\s*,\\s*\\d{6}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter exactly two six-digit PIN codes separated by a comma."
            );
        }

        Rider rider = getRider(riderId);

        rider.updateProfile(
                name,
                phone,
                vehicle,
                vehicleNumber,
                licenseNumber,
                city,
                servicePinCodes.replaceAll("\\s*,\\s*", ",")
        );

        return toView(riderRepository.save(rider));
    }

    @Transactional
    public RiderProfileView updateStatus(String riderId, String value) {
        Rider rider = getRider(riderId);

        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status is required."
            );
        }

        String requestedStatus = value.trim().toUpperCase();

        if (!requestedStatus.equals("ONLINE")
                && !requestedStatus.equals("OFFLINE")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be ONLINE or OFFLINE."
            );
        }

        String currentStatus = rider.getStatus() == null
                ? "OFFLINE"
                : rider.getStatus().trim().toUpperCase();

        if ("ON_DELIVERY".equals(currentStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You cannot change availability while a delivery is in progress."
            );
        }

        if ("SUSPENDED".equals(currentStatus)
                || "REJECTED".equals(currentStatus)
                || "PENDING".equals(currentStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Your rider account is not active."
            );
        }

        rider.setStatus(requestedStatus);

        return toView(riderRepository.save(rider));
    }

    private Rider getRider(String riderId) {
        return riderRepository.findById(riderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rider account not found."
                ));
    }

    private RiderProfileView toView(Rider rider) {
        return new RiderProfileView(
                rider.getId(),
                rider.getName(),
                rider.getEmail(),
                rider.getPhone(),
                rider.getVehicle(),
                rider.getVehicleNumber(),
                rider.getLicenseNumber(),
                rider.getCity(),
                rider.getServicePinCodes(),
                rider.getStatus(),
                rider.getRating(),
                rider.getTotalDeliveries(),
                rider.getEarnings()
        );
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
