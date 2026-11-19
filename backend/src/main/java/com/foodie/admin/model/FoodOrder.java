package com.foodie.admin.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Entity
@Table(name = "food_orders")
public class FoodOrder {

    @Id
    private String id;
    private String customerId;
    private String customerName;
    private String sellerId;
    private String sellerName;
    private String riderId;
    private String riderName;
    private String status;
    private double amount;
    private String payment;
    private String deliveryAddress;
    private Instant createdAt;
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "food_order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<FoodOrderItem> items = new ArrayList<>();

    protected FoodOrder() {
    }

    public FoodOrder(String id, String customerId, String customerName, String sellerId, String sellerName,
                     double amount, String payment, String address, List<FoodOrderItem> items) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.amount = amount;
        this.payment = payment;
        this.deliveryAddress = address;
        this.items = new ArrayList<>(items);
        this.status = "PLACED";
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public String getRiderId() { return riderId; }
    public String getRiderName() { return riderName; }
    public String getStatus() { return status; }
    public double getAmount() { return amount; }
    public String getPayment() { return payment; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<FoodOrderItem> getItems() { return items; }

    public Order toOrder() {
        String itemSummary = items.stream()
                .map(item -> item.getQuantity() + "× " + item.getName())
                .collect(Collectors.joining(", "));
        String placedAt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)
                .withZone(ZoneId.systemDefault()).format(createdAt);
        return new Order(id, customerId, customerName, sellerId, sellerName, riderId, riderName,
                itemSummary, amount, status, payment, deliveryAddress, placedAt);
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void assignRider(String id, String name) {
        this.riderId = id;
        this.riderName = name;
        setStatus("RIDER_ASSIGNED");
    }
}
