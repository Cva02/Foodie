package com.foodie.admin.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
    name = "rider_order_declines",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"rider_id", "order_id"}
    )
)
public class RiderOrderDecline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rider_id", nullable = false, length = 20)
    private String riderId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Instant declinedAt = Instant.now();

    protected RiderOrderDecline() {
    }

    public RiderOrderDecline(String riderId, Long orderId) {
        this.riderId = riderId;
        this.orderId = orderId;
    }
}