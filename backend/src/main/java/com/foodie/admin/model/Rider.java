package com.foodie.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "riders")
public class Rider {

    public static final double DEFAULT_RATING = 5.0;

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
    @Column(length = 13)
    private String servicePinCodes;
    private String status;
    private double rating;
    private int totalDeliveries;
    private double earnings;
    private String joinedOn;
    private Double latitude;
    private Double longitude;

    protected Rider() {
    }

    public Rider(String id, String name, String avatar, String phone, String email, String vehicle,
                 String vehicleNumber, String licenseNumber, String city, String servicePinCodes,
                 String status, double rating, int totalDeliveries, double earnings, String joinedOn) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.phone = phone;
        this.email = email;
        this.vehicle = vehicle;
        this.vehicleNumber = vehicleNumber;
        this.licenseNumber = licenseNumber;
        this.city = city;
        this.servicePinCodes = servicePinCodes;
        this.status = status;
        this.rating = rating;
        this.totalDeliveries = totalDeliveries;
        this.earnings = earnings;
        this.joinedOn = joinedOn;
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
    public String getServicePinCodes() { return servicePinCodes; }
    public String getStatus() { return status; }
    public double getRating() { return rating; }
    public int getTotalDeliveries() { return totalDeliveries; }
    public double getEarnings() { return earnings; }
    public String getJoinedOn() { return joinedOn; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    public void setStatus(String status) { this.status = status; }
    public void setRating(double rating) { this.rating = rating; }
    public void setServicePinCodes(String servicePinCodes) { this.servicePinCodes = servicePinCodes; }

    public void updateProfile(String name, String phone, String vehicle, String vehicleNumber,
                              String licenseNumber, String city, String servicePinCodes) {
        this.name = name;
        this.phone = phone;
        this.vehicle = vehicle;
        this.vehicleNumber = vehicleNumber;
        this.licenseNumber = licenseNumber;
        this.city = city;
        this.servicePinCodes = servicePinCodes;
    }

    public boolean servesPinCode(String pinCode) {
        if (pinCode == null || servicePinCodes == null) return false;
        for (String servicePinCode : servicePinCodes.split(",")) {
            if (servicePinCode.trim().equals(pinCode.trim())) return true;
        }
        return false;
    }

    public void updateLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void completeDelivery(double earned) {
        this.totalDeliveries++;
        this.earnings += earned;
    }
}
