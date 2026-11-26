package com.foodie.admin.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** A restaurant / seller (MySQL table `sellers`). */
@Entity
@Table(name = "sellers")
public class Seller {

    @Id
    @Column(length = 20)
    private String id;
    @Column(name = "restaurant_id")
private Long restaurantId;
    private String name;            // restaurant name
    private String ownerName;
    @Column(length = 1000)
    private String ownerAvatar;
    @Column(length = 1000)
    private String image;           // cover image
    private String cuisine;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String fssaiNumber;
    private double rating;
    private String status;          // ACTIVE | PENDING | SUSPENDED
    @Column(length = 1000)
    private String statusNote;
    @Column(name = "is_open")
    private boolean open;
    private String joinedOn;
    private int totalOrders;
    private double revenue;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "seller_menu_items", joinColumns = @JoinColumn(name = "seller_id"))
    private List<MenuItem> menu = new ArrayList<>();

    protected Seller() {
    }

    public Seller(String id, String name, String ownerName, String ownerAvatar, String image, String cuisine,
                  String phone, String email, String address, String city, String fssaiNumber, double rating,
                  String status, boolean open, String joinedOn, int totalOrders, double revenue,
                  List<MenuItem> menu) {
        this.id = id;
        this.name = name;
        this.ownerName = ownerName;
        this.ownerAvatar = ownerAvatar;
        this.image = image;
        this.cuisine = cuisine;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.city = city;
        this.fssaiNumber = fssaiNumber;
        this.rating = rating;
        this.status = status;
        this.open = open;
        this.joinedOn = joinedOn;
        this.totalOrders = totalOrders;
        this.revenue = revenue;
        this.menu = menu == null ? new ArrayList<>() : new ArrayList<>(menu);
    }

    public String getId() { return id; }
    public Long getRestaurantId() { return restaurantId; }
public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public String getName() { return name; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerAvatar() { return ownerAvatar; }
    public String getImage() { return image; }
    public String getCuisine() { return cuisine; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getFssaiNumber() { return fssaiNumber; }
    public double getRating() { return rating; }
    public String getStatus() { return status; }
    public String getStatusNote() { return statusNote; }
    public boolean isOpen() { return open; }
    public String getJoinedOn() { return joinedOn; }
    public int getTotalOrders() { return totalOrders; }
    public double getRevenue() { return revenue; }
    public List<MenuItem> getMenu() { return menu; }

    public void addMenuItem(MenuItem item) { this.menu.add(item); }
    public boolean removeMenuItem(String name) {
        return this.menu.removeIf(item -> item.name().equalsIgnoreCase(name));
    }

    public void setName(String name) { this.name = name; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setOwnerAvatar(String ownerAvatar) { this.ownerAvatar = ownerAvatar; }
    public void setImage(String image) { this.image = image; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setCity(String city) { this.city = city; }
    public void setFssaiNumber(String fssaiNumber) { this.fssaiNumber = fssaiNumber; }
    public void setStatus(String status) { this.status = status; }
    public void setStatusNote(String statusNote) { this.statusNote = statusNote; }
    public void setOpen(boolean open) { this.open = open; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    public void setRevenue(double revenue) { this.revenue = revenue; }
}
