package com.foodie.customer.web;

import com.foodie.customer.dto.Dtos.*;
import com.foodie.customer.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurants;

    @GetMapping
    public List<RestaurantDto> list(@RequestParam(required = false) String q,
                                    @RequestParam(defaultValue = "false") boolean veg,
                                    @RequestParam(required = false) String cuisine,
                                    @RequestParam(required = false) String sort,
                                    @RequestParam(required = false) Double lat,
                                    @RequestParam(required = false) Double lon) {
        return restaurants.list(q, veg, cuisine, sort, lat, lon);
    }

    @GetMapping("/{id}")
    public RestaurantDetail detail(@PathVariable Long id,
                                   @RequestParam(required = false) Double lat,
                                   @RequestParam(required = false) Double lon) {
        return restaurants.detail(id, lat, lon);
    }
}
