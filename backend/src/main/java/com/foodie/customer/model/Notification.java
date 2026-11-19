package com.foodie.customer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

/** Either userId (customer) or restaurantId (seller) is set, so the same table feeds both portals. */
@Entity @Table(name = "notifications")
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long restaurantId;
    private Long orderId;
    private String type;
    private String title;
    @Column(length = 500)
    private String message;
    @Column(name = "is_read")
    private boolean read;
    private Instant createdAt = Instant.now();
}
