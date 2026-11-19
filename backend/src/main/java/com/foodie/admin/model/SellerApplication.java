package com.foodie.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "seller_applications")
public class SellerApplication {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private String restaurantName;
    @Column(nullable = false)
    private String ownerName;
    @Column(nullable = false)
    private String cuisine;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false, length = 1000)
    private String address;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String fssaiNumber;
    @Column(length = 1000)
    private String image;
    private String fssaiDocumentFileName;

    @Column(nullable = false)
    private String status = "PENDING";
    @Column(length = 1000)
    private String reviewNote;
    private String sellerId;

    @Column(nullable = false)
    private Instant submittedAt;

    protected SellerApplication() {
    }

    public SellerApplication(String id, String email, String passwordHash, String restaurantName,
                             String ownerName, String cuisine, String phone, String address,
                             String city, String fssaiNumber, String image, String fssaiDocumentFileName) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.restaurantName = restaurantName;
        this.ownerName = ownerName;
        this.cuisine = cuisine;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.fssaiNumber = fssaiNumber;
        this.image = image;
        this.fssaiDocumentFileName = fssaiDocumentFileName;
        this.submittedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRestaurantName() { return restaurantName; }
    public String getOwnerName() { return ownerName; }
    public String getCuisine() { return cuisine; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getFssaiNumber() { return fssaiNumber; }
    public String getImage() { return image; }
    public String getFssaiDocumentFileName() { return fssaiDocumentFileName; }
    public String getStatus() { return status; }
    public String getReviewNote() { return reviewNote; }
    public String getSellerId() { return sellerId; }
    public Instant getSubmittedAt() { return submittedAt; }

    public void resubmit(String passwordHash, String restaurantName, String ownerName,
                         String cuisine, String phone, String address, String city,
                         String fssaiNumber, String image, String fssaiDocumentFileName) {
        this.passwordHash = passwordHash;
        this.restaurantName = restaurantName;
        this.ownerName = ownerName;
        this.cuisine = cuisine;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.fssaiNumber = fssaiNumber;
        this.image = image;
        this.fssaiDocumentFileName = fssaiDocumentFileName;
        this.status = "PENDING";
        this.reviewNote = null;
        this.sellerId = null;
        this.submittedAt = Instant.now();
    }

    public void approve(String sellerId) {
        this.status = "APPROVED";
        this.sellerId = sellerId;
        this.passwordHash = null;
    }

    public void reject(String note) {
        this.status = "REJECTED";
        this.reviewNote = note;
        this.passwordHash = null;
    }
}
