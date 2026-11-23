package com.foodie.admin.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "rider_declined_orders",
        uniqueConstraints = @UniqueConstraint(columnNames = {"rider_id", "order_id"}),
        indexes = @Index(name = "idx_rider_declined_orders_rider", columnList = "rider_id"))
public class RiderDeclinedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String riderId;
    private String orderId;

    protected RiderDeclinedOrder() {
    }

    public RiderDeclinedOrder(String riderId, String orderId) {
        this.riderId = riderId;
        this.orderId = orderId;
    }

    public String getRiderId() { return riderId; }
    public String getOrderId() { return orderId; }
}
