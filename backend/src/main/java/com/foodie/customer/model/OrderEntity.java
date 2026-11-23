package com.foodie.customer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "orders")
@Getter @Setter @NoArgsConstructor
public class OrderEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long restaurantId;
    private String restaurantName;
    private String riderId;
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PLACED;
    private int itemTotal;
    private int deliveryFee;
    private int taxes;
    private Integer discount = 0;
    private String couponCode;
    private int total;
    private String paymentMethod;
    @Column(length = 500)
    private String address;
    private Double lat;
    private Double lon;
    @Column(length = 500)
    private String instructions;
    private Instant createdAt = Instant.now();
    private Instant statusUpdatedAt = Instant.now();
    private Instant cancelledAt;
    private String cancelReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    public int getDiscount() {
        return discount == null ? 0 : discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount == null ? 0 : discount;
    }

    public void setStatusNow(OrderStatus s) {
        this.status = s;
        this.statusUpdatedAt = Instant.now();
    }
}
