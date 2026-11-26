package com.foodie.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "rider_applications")
public class RiderApplication {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String vehicle;
    @Column(nullable = false)
    private String vehicleNumber;
    @Column(nullable = false)
    private String licenseNumber;
    @Column(nullable = false)
    private String city;

    @Column(length = 13)
private String servicePinCodes;

    private String photoFileName;
    private String licenseFileName;

    @Column(nullable = false)
    private String status = "PENDING";
    @Column(length = 1000)
    private String reviewNote;
    private String riderId;

    @Column(nullable = false)
    private Instant submittedAt;

    protected RiderApplication() {
    }

    public RiderApplication(String id, String email, String passwordHash, String name, String phone,
                        String vehicle, String vehicleNumber, String licenseNumber, String city,
                        String servicePinCodes, String photoFileName, String licenseFileName) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.phone = phone;
        this.vehicle = vehicle;
        this.vehicleNumber = vehicleNumber;
        this.licenseNumber = licenseNumber;
        this.city = city;
        this.photoFileName = photoFileName;
	this.servicePinCodes = servicePinCodes;
        this.licenseFileName = licenseFileName;
        this.submittedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getVehicle() { return vehicle; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getCity() { return city; }
    public String getPhotoFileName() { return photoFileName; }
    public String getServicePinCodes() { return servicePinCodes; }
    public String getLicenseFileName() { return licenseFileName; }
    public String getStatus() { return status; }
    public String getReviewNote() { return reviewNote; }
    public String getRiderId() { return riderId; }
    public Instant getSubmittedAt() { return submittedAt; }

    public void resubmit(String passwordHash, String name, String phone, String vehicle,
                     String vehicleNumber, String licenseNumber, String city,
                     String servicePinCodes, String photoFileName, String licenseFileName) {

        this.passwordHash = passwordHash;
        this.name = name;
        this.phone = phone;
        this.vehicle = vehicle;
        this.vehicleNumber = vehicleNumber;
        this.licenseNumber = licenseNumber;
        this.city = city;
        this.photoFileName = photoFileName;
	this.servicePinCodes = servicePinCodes;
        this.licenseFileName = licenseFileName;
        this.status = "PENDING";
        this.reviewNote = null;
        this.riderId = null;
        this.submittedAt = Instant.now();
    }

    public void approve(String riderId) {
        this.status = "APPROVED";
        this.riderId = riderId;
        this.passwordHash = null;
    }

    public void reject(String note) {
        this.status = "REJECTED";
        this.reviewNote = note;
        this.passwordHash = null;
    }
}
