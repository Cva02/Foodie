package com.foodie.admin.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Entity
@Table(name = "food_orders")
public class FoodOrder {

    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    @Id
    private String id;
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String sellerId;
    private String sellerName;
    private String sellerAddress;
    @Column(length = 6)
    private String customerPinCode;
    private Double sellerLatitude;
    private Double sellerLongitude;
    private Double customerLatitude;
    private Double customerLongitude;
    private String riderId;
    private String riderName;
    private String status;
    private double amount;
    private String payment;
    private String deliveryAddress;
    @Column(unique = true, length = 64)
    private String trackingToken;
    @Column(length = 6)
    private String pickupOtp;
    @Column(length = 6)
    private String deliveryOtp;
    private Instant expectedDeliveryAt;
    private Instant deliveredAt;
    private boolean demoOrder;
    private Instant createdAt;
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "food_order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<FoodOrderItem> items = new ArrayList<>();

    protected FoodOrder() {
    }

    public FoodOrder(String id, String customerId, String customerName, String sellerId, String sellerName,
                     String customerPhone, double amount, String payment, String address,
                     List<FoodOrderItem> items) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.amount = amount;
        this.payment = payment;
        this.deliveryAddress = address;
        this.items = new ArrayList<>(items);
        this.pickupOtp = generateOtp();
        this.deliveryOtp = generateOtp();
        this.status = "PLACED";
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public static FoodOrder readyOrder(String id, String customerName, String customerPhone,
                                       String sellerId, String sellerName, String sellerAddress,
                                       String deliveryAddress, String customerPinCode,
                                       Double customerLatitude, Double customerLongitude,
                                       Double sellerLatitude, Double sellerLongitude, double amount, String payment,
                                       List<FoodOrderItem> items, Instant createdAt, Instant expectedDeliveryAt,
                                       String trackingToken, boolean demoOrder) {
        FoodOrder order = new FoodOrder();
        order.id = id;
        order.customerName = customerName;
        order.customerPhone = customerPhone;
        order.sellerId = sellerId;
        order.sellerName = sellerName;
        order.sellerAddress = sellerAddress;
        order.deliveryAddress = deliveryAddress;
        order.customerPinCode = customerPinCode;
        order.customerLatitude = customerLatitude;
        order.customerLongitude = customerLongitude;
        order.sellerLatitude = sellerLatitude;
        order.sellerLongitude = sellerLongitude;
        order.status = "READY";
        order.amount = amount;
        order.payment = payment;
        order.items = new ArrayList<>(items);
        order.createdAt = createdAt;
        order.updatedAt = createdAt;
        order.expectedDeliveryAt = expectedDeliveryAt;
        order.trackingToken = trackingToken;
        order.demoOrder = demoOrder;
        order.ensureOtpCodes();
        return order;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public String getRiderId() { return riderId; }
    public String getRiderName() { return riderName; }
    public String getSellerAddress() { return sellerAddress; }
    public String getCustomerPinCode() { return customerPinCode; }
    public Double getSellerLatitude() { return sellerLatitude; }
    public Double getSellerLongitude() { return sellerLongitude; }
    public Double getCustomerLatitude() { return customerLatitude; }
    public Double getCustomerLongitude() { return customerLongitude; }
    public String getStatus() { return status; }
    public double getAmount() { return amount; }
    public String getPayment() { return payment; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getTrackingToken() { return trackingToken; }
    public String getPickupOtp() { return pickupOtp; }
    public String getDeliveryOtp() { return deliveryOtp; }
    public Instant getExpectedDeliveryAt() { return expectedDeliveryAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public boolean isDemoOrder() { return demoOrder; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<FoodOrderItem> getItems() { return items; }

    public Order toOrder() {
        return toOrder(null);
    }

    public Order toOrder(Double distanceKm) {
        String itemSummary = getItemsSummary();
        String placedAt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)
                .withZone(ZoneId.systemDefault()).format(createdAt);
        String expectedAt = formatTime(expectedDeliveryAt);
        String deliveredAtText = formatTime(deliveredAt);
        return new Order(id, customerId, customerName, sellerId, sellerName, riderId, riderName,
                customerPhone, itemSummary, amount, status, payment, sellerAddress, deliveryAddress,
                customerPinCode, placedAt, expectedAt, deliveredAtText, distanceKm, sellerLatitude, sellerLongitude,
                customerLatitude, customerLongitude);
    }

    public Order toOrderWithoutCustomerPhone() {
        Order order = toOrder();
        return new Order(order.id(), order.customerId(), order.customerName(), order.sellerId(), order.sellerName(),
                order.riderId(), order.riderName(), null, order.items(), order.amount(), order.status(),
                order.payment(), order.sellerAddress(), order.address(), order.customerPinCode(), order.placedAt(),
                order.expectedDeliveryAt(), order.deliveredAt(), order.distanceKm(), order.sellerLatitude(),
                order.sellerLongitude(), order.customerLatitude(), order.customerLongitude());
    }

    public Order toAvailableOrder(Double distanceKm) {
        Order order = toOrder(distanceKm);
        return new Order(order.id(), null, "", order.sellerId(), order.sellerName(),
                null, null, null, order.items(), order.amount(), order.status(), order.payment(),
                order.sellerAddress(), "", order.customerPinCode(), order.placedAt(), order.expectedDeliveryAt(),
                null, distanceKm,
                order.sellerLatitude(), order.sellerLongitude(), null, null);
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = Instant.now();
        if ("DELIVERED".equals(status)) deliveredAt = updatedAt;
    }

    public boolean ensureOtpCodes() {
        boolean updated = false;
        if (pickupOtp == null || pickupOtp.isBlank()) {
            pickupOtp = generateOtp();
            updated = true;
        }
        if (deliveryOtp == null || deliveryOtp.isBlank()) {
            deliveryOtp = generateOtp();
            updated = true;
        }
        return updated;
    }

    public void updateDemoLocation(String sellerName, String sellerAddress, String deliveryAddress,
                                   String customerPinCode, Double customerLatitude, Double customerLongitude,
                                   Double sellerLatitude, Double sellerLongitude) {
        this.sellerName = sellerName;
        this.sellerAddress = sellerAddress;
        this.deliveryAddress = deliveryAddress;
        this.customerPinCode = customerPinCode;
        this.customerLatitude = customerLatitude;
        this.customerLongitude = customerLongitude;
        this.sellerLatitude = sellerLatitude;
        this.sellerLongitude = sellerLongitude;
        this.updatedAt = Instant.now();
    }

    public void assignRider(String id, String name) {
        this.riderId = id;
        this.riderName = name;
        setStatus("RIDER_ASSIGNED");
    }

    public String getItemsSummary() {
        return items.stream()
                .map(item -> item.getQuantity() + "× " + item.getName())
                .collect(Collectors.joining(", "));
    }

    private static String formatTime(Instant instant) {
        return instant == null ? null : DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)
                .withZone(ZoneId.systemDefault()).format(instant);
    }

    private static String generateOtp() {
        return String.format(Locale.ROOT, "%06d", OTP_RANDOM.nextInt(1_000_000));
    }
}
