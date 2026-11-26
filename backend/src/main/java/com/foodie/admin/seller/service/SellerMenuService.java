package com.foodie.admin.seller.service;

import com.foodie.admin.model.Seller;
import com.foodie.admin.repository.SellerRepository;
import com.foodie.admin.seller.dto.SellerMenuDtos.MenuItemRequest;
import com.foodie.admin.seller.dto.SellerMenuDtos.MenuItemView;
import com.foodie.customer.model.MenuItem;
import com.foodie.customer.repo.Repos.MenuItemRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class SellerMenuService {

    private final SellerRepository sellers;
    private final MenuItemRepo menuItems;

    @Transactional(readOnly = true)
    public List<MenuItemView> list(String sellerId) {
        Seller seller = activeSeller(sellerId);

        return menuItems.findByRestaurantId(seller.getRestaurantId())
                .stream()
                .map(this::toView)
                .toList();
    }

    public MenuItemView add(String sellerId, MenuItemRequest request) {
        Seller seller = activeSeller(sellerId);
        validate(request);

        MenuItem item = new MenuItem();
        item.setRestaurantId(seller.getRestaurantId());
        apply(item, request);

        return toView(menuItems.save(item));
    }

    public MenuItemView update(
            String sellerId,
            Long itemId,
            MenuItemRequest request) {

        Seller seller = activeSeller(sellerId);
        validate(request);

        MenuItem item = ownedItem(seller, itemId);
        apply(item, request);

        return toView(menuItems.save(item));
    }

    public MenuItemView updateAvailability(
            String sellerId,
            Long itemId,
            boolean inStock) {

        Seller seller = activeSeller(sellerId);
        MenuItem item = ownedItem(seller, itemId);

        item.setAvailable(inStock);

        return toView(menuItems.save(item));
    }

    public void delete(String sellerId, Long itemId) {
        Seller seller = activeSeller(sellerId);
        MenuItem item = ownedItem(seller, itemId);
        menuItems.delete(item);
    }

    private Seller activeSeller(String sellerId) {
        Seller seller = sellers.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Seller not found"));

        if (!"ACTIVE".equalsIgnoreCase(seller.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Your seller account is not active");
        }

        if (seller.getRestaurantId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Your restaurant is not linked yet. Contact admin.");
        }

        return seller;
    }

    private MenuItem ownedItem(Seller seller, Long itemId) {
        MenuItem item = menuItems.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Menu item not found"));

        if (!seller.getRestaurantId().equals(item.getRestaurantId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot modify another restaurant's menu item");
        }

        return item;
    }

    private void validate(MenuItemRequest request) {
        if (request == null
                || request.name() == null
                || request.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Dish name is required");
        }

        if (request.name().trim().length() > 150) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Dish name is too long");
        }

        if (request.price() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Price cannot be negative");
        }

        if (request.category() == null
                || request.category().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Dish category is required");
        }

        if (request.description() != null
                && request.description().length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Description is too long");
        }
    }

    private void apply(MenuItem item, MenuItemRequest request) {
        item.setName(request.name().trim());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setVeg(request.veg());
        item.setCategory(request.category().trim());
        item.setBestseller(request.bestseller());
        item.setAvailable(request.inStock());
        item.setImageUrl(request.imageUrl());
        item.setEmoji(request.emoji());
    }

    private MenuItemView toView(MenuItem item) {
        return new MenuItemView(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.isVeg(),
                item.getCategory(),
                item.isBestseller(),
                item.isAvailable(),
                item.getImageUrl(),
                item.getEmoji()
        );
    }
}