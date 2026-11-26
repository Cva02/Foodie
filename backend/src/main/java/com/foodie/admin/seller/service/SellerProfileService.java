
package com.foodie.admin.seller.service;

import com.foodie.admin.model.Seller;
import com.foodie.admin.repository.SellerRepository;
import com.foodie.admin.seller.dto.SellerProfileDtos.ProfileView;
import com.foodie.customer.repo.Repos.RestaurantRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SellerProfileService {

    private final SellerRepository sellers;
    private final RestaurantRepo restaurants;

    public SellerProfileService(SellerRepository sellers, RestaurantRepo restaurants) {
        this.sellers = sellers;
        this.restaurants = restaurants;
    }

    public ProfileView getProfile(String sellerId) {
        Seller seller = sellers.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Seller profile not found"));

        return toView(seller);
    }

    @Transactional
    public ProfileView updateAvailability(String sellerId, boolean open) {
        Seller seller = sellers.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Seller profile not found"));

        if (!"ACTIVE".equalsIgnoreCase(seller.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only active restaurants can update availability");
        }

        seller.setOpen(open);
        Seller saved = sellers.save(seller);

        if (saved.getRestaurantId() != null) {
            restaurants.findById(saved.getRestaurantId()).ifPresent(restaurant -> {
                restaurant.setOpen(open);
                restaurants.save(restaurant);
            });
        }

        return toView(saved);
    }

    private ProfileView toView(Seller seller) {
        return new ProfileView(
                seller.getId(),
                seller.getName(),
                seller.getOwnerName(),
                seller.getEmail(),
                seller.getPhone(),
                seller.getCuisine(),
                seller.getAddress(),
                seller.getCity(),
                seller.getFssaiNumber(),
                seller.getImage(),
                seller.getStatus(),
                seller.getStatusNote(),
                seller.isOpen()
        );
    }
}
