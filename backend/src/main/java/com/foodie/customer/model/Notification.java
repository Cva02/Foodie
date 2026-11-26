package com.foodie.customer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** A notification belongs to a customer, seller, or rider. */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long restaurantId;

    @Column(length = 20)
    private String riderId;

    private Long orderId;
    private String type;
    private String title;

    @Column(length = 500)
    private String message;

    @Column(name = "is_read")
    private boolean read;

    private Instant createdAt = Instant.now();
}