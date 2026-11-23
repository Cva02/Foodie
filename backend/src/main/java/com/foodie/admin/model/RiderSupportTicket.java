package com.foodie.admin.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.Instant;

@Entity
@Table(name = "rider_support_tickets",
        indexes = @Index(name = "idx_rider_support_tickets_rider_created", columnList = "rider_id, created_at"))
public class RiderSupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20)
    private String riderId;
    @Column(nullable = false, length = 120)
    private String subject;
    @Column(nullable = false, length = 2000)
    private String message;
    @Column(nullable = false, length = 30)
    private String status;
    @Column(nullable = false)
    private Instant createdAt;

    protected RiderSupportTicket() {
    }

    public RiderSupportTicket(String riderId, String subject, String message) {
        this.riderId = riderId;
        this.subject = subject;
        this.message = message;
        this.status = "OPEN";
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getRiderId() { return riderId; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
