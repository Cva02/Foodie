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

/** A delivery rider (MySQL table `riders`). */
@Entity
@Table(name = "riders")
public class Rider {

    @Id
    @Column(length = 20)
    private String id;
    private String name;
    @Column(length = 1000)
    private String avatar;
    private String phone;
    private String email;
    private String vehicle;
    private String vehicleNumber;
    private String licenseNumber;
    private String city;
    private String status;          // ONLINE | ON_DELIVERY | OFFLINE | PENDING | REJECTED | SUSPENDED
    @Column(length = 1000)
    private String statusNote;
    private double rating;
    private int totalDeliveries;
    private double earnings;
    private String joinedOn;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rider_documents", joinColumns = @JoinColumn(name = "rider_id"))
    private List<RiderDocument> documents = new ArrayList<>();

    protected Rider() {
    }

    public Rider(String id, String name, String avatar, String phone, String email, String vehicle,
                 String vehicleNumber, String licenseNumber, String city, String status, double rating,
                 int totalDeliveries, double earnings, String joinedOn, List<RiderDocument> documents) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.phone = phone;
        this.email = email;
        this.vehicle = vehicle;
        this.vehicleNumber = vehicleNumber;
        this.licenseNumber = licenseNumber;
        this.city = city;
        this.status = status;
        this.rating = rating;
        this.totalDeliveries = totalDeliveries;
        this.earnings = earnings;
        this.joinedOn = joinedOn;
        this.documents = documents == null ? new ArrayList<>() : new ArrayList<>(documents);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAvatar() { return avatar; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getVehicle() { return vehicle; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getCity() { return city; }
    public String getStatus() { return status; }
    public String getStatusNote() { return statusNote; }
    public double getRating() { return rating; }
    public int getTotalDeliveries() { return totalDeliveries; }
    public double getEarnings() { return earnings; }
    public String getJoinedOn() { return joinedOn; }
    public List<RiderDocument> getDocuments() { return documents; }

    public void setName(String name) { this.name = name; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setVehicle(String vehicle) { this.vehicle = vehicle; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public void setCity(String city) { this.city = city; }
    public void setStatus(String status) { this.status = status; }
    public void setStatusNote(String statusNote) { this.statusNote = statusNote; }
}
