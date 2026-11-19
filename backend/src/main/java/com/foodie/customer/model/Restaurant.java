package com.foodie.customer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "restaurants")
@Getter @Setter @NoArgsConstructor
public class Restaurant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String cuisines;          // comma separated
    private String emoji;             // placeholder artwork when imageUrl is empty
    private String imageUrl;
    private double rating;
    private int deliveryMinutes;
    private int costForTwo;
    private boolean pureVeg;
    private String area;
    private double lat;
    private double lon;
    private boolean open = true;
    private String offer;
}
