package com.foodie.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "portal_accounts")
public class PortalAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role;

    private String displayName;
    private String profileId;
    private String phone;
    private String status = "ACTIVE";

    protected PortalAccount() {
    }

    public PortalAccount(String email, String passwordHash, String role, String displayName,
                         String profileId, String phone) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.displayName = displayName;
        this.profileId = profileId;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public String getDisplayName() { return displayName; }
    public String getProfileId() { return profileId; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
