package com.foodie.admin.rider.service;

import com.foodie.admin.model.Rider;
import com.foodie.admin.repository.RiderRepository;
import com.foodie.admin.rider.dto.RiderLocationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RiderLocationService {

    private final RiderRepository riderRepository;

    @Transactional
    public void updateLocation(String riderId, RiderLocationRequest request) {

        if (request == null
                || request.latitude() == null
                || request.longitude() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude and longitude are required."
            );
        }

        double latitude = request.latitude();
        double longitude = request.longitude();

        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid GPS coordinates."
            );
        }

        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rider account not found."
                ));

        if (!"ONLINE".equals(rider.getStatus())
                && !"ON_DELIVERY".equals(rider.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Go online before sharing your location."
            );
        }

        rider.updateLocation(latitude, longitude);
        riderRepository.save(rider);
    }
}