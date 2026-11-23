package com.foodie.admin.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "delivery_locations", indexes = {
        @Index(name = "idx_delivery_locations_order_time", columnList = "order_id,recorded_at")
})
public class DeliveryLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private String riderId;
    private double latitude;
    private double longitude;
    private Instant recordedAt;

    protected DeliveryLocation() {
    }

    public DeliveryLocation(String orderId, String riderId, double latitude, double longitude, Instant recordedAt) {
        this.orderId = orderId;
        this.riderId = riderId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordedAt = recordedAt;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getOrderId() { return orderId; }
    public String getRiderId() { return riderId; }
    public Instant getRecordedAt() { return recordedAt; }
}
