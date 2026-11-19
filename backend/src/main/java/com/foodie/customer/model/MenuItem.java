package com.foodie.customer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "menu_items")
@Getter @Setter @NoArgsConstructor
public class MenuItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long restaurantId;
    private String name;
    @Column(length = 500)
    private String description;
    private int price;
    private boolean veg;
    private String category;
    private boolean bestseller;
    private boolean available = true;
    private String emoji;
    private String imageUrl;
}
