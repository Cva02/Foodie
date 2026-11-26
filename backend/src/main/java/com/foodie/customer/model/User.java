package com.foodie.customer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    private String phone;
    @Column(nullable = false)
    private String passwordHash;
    private Double lat;
    private Double lon;
    private String locationLabel;
    private String status = "ACTIVE";
    private Instant createdAt = Instant.now();

    public String getStatus() {
        return status == null || status.isBlank() ? "ACTIVE" : status;
    }
}
