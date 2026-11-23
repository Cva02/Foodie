package com.foodie.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "rider_notifications",
        indexes = @Index(name = "idx_rider_notifications_rider_created", columnList = "rider_id, created_at"))
public class RiderNotification {

    public static final String ORDER_ACCEPTED = "ORDER_ACCEPTED";
    public static final String ORDER_DELIVERED = "ORDER_DELIVERED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20)
    private String riderId;
    @Column(nullable = false, length = 64)
    private String orderId;
    @Column(nullable = false, length = 30)
    private String type;
    @Column(nullable = false, length = 200)
    private String message;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant readAt;

    protected RiderNotification() {
    }

    public RiderNotification(String riderId, String orderId, String type, String message) {
        this.riderId = riderId;
        this.orderId = orderId;
        this.type = type;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getRiderId() { return riderId; }
    public String getOrderId() { return orderId; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReadAt() { return readAt; }

    public void markRead() {
        if (readAt == null) readAt = Instant.now();
    }
}
