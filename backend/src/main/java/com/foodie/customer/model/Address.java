package com.foodie.customer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "addresses")
@Getter @Setter @NoArgsConstructor
public class Address {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    private String label;
    @Column(length = 500)
    private String line;
    @Column(length = 6)
    private String pinCode;
    private Double lat;
    private Double lon;
}
