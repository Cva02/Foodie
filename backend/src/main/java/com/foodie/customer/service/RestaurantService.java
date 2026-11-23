package com.foodie.customer.service;

import com.foodie.customer.dto.Dtos.*;
import com.foodie.customer.model.MenuItem;
import com.foodie.customer.model.Restaurant;
import com.foodie.customer.repo.Repos.MenuItemRepo;
import com.foodie.customer.repo.Repos.RestaurantRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepo restaurants;
    private final MenuItemRepo menu;

    @Transactional(readOnly = true)
    public List<RestaurantDto> list(String q, boolean veg, String cuisine, String sort, Double lat, Double lon) {
        String query = q == null ? "" : q.trim().toLowerCase();
        Set<Long> dishMatches = query.isEmpty() ? Set.of()
                : menu.findByNameContainingIgnoreCase(query).stream().map(MenuItem::getRestaurantId).collect(Collectors.toSet());
        return restaurants.findAll().stream()
                .filter(r -> !veg || r.isPureVeg())
                .filter(r -> cuisine == null || cuisine.isBlank() || r.getCuisines().toLowerCase().contains(cuisine.toLowerCase()))
                .filter(r -> query.isEmpty() || r.getName().toLowerCase().contains(query)
                        || r.getCuisines().toLowerCase().contains(query) || dishMatches.contains(r.getId()))
                .map(r -> dto(r, lat, lon))
                .sorted(comparator(sort))
                .toList();
    }

    @Transactional(readOnly = true)
    public RestaurantDetail detail(Long id, Double lat, Double lon) {
        Restaurant r = restaurants.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
        List<MenuItemDto> items = menu.findByRestaurantId(id).stream().map(this::dto).toList();
        return new RestaurantDetail(dto(r, lat, lon), items);
    }

    private Comparator<RestaurantDto> comparator(String sort) {
        Comparator<RestaurantDto> byOpen = Comparator.comparing((RestaurantDto d) -> !d.open());
        if (sort == null) sort = "";
        return switch (sort) {
            case "rating" -> byOpen.thenComparing(Comparator.comparingDouble(RestaurantDto::rating).reversed());
            case "time" -> byOpen.thenComparingInt(RestaurantDto::deliveryMinutes);
            case "costLow" -> byOpen.thenComparingInt(RestaurantDto::costForTwo);
            case "costHigh" -> byOpen.thenComparing(Comparator.comparingInt(RestaurantDto::costForTwo).reversed());
            case "distance" -> byOpen.thenComparing(Comparator.comparing(RestaurantDto::distanceKm,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            default -> byOpen.thenComparing(Comparator.comparingDouble(RestaurantDto::rating).reversed());
        };
    }

    private RestaurantDto dto(Restaurant r, Double lat, Double lon) {
        Double km = (lat == null || lon == null) ? null : Math.round(haversine(lat, lon, r.getLat(), r.getLon()) * 10) / 10.0;
        return new RestaurantDto(r.getId(), r.getName(), r.getCuisines(), r.getEmoji(), r.getImageUrl(), r.getRating(),
                r.getDeliveryMinutes(), r.getCostForTwo(), r.isPureVeg(), r.getArea(), r.isOpen(), r.getOffer(), km);
    }

    private MenuItemDto dto(MenuItem m) {
        return new MenuItemDto(m.getId(), m.getName(), m.getDescription(), m.getPrice(), m.isVeg(), m.getCategory(),
                m.isBestseller(), m.isAvailable(), m.getEmoji(), m.getImageUrl());
    }

    static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
