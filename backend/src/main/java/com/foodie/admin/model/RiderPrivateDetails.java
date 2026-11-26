package com.foodie.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rider_private_details")
public class RiderPrivateDetails {

    @Id
    @Column(length = 20)
    private String riderId;

    @Column(nullable = false, length = 100)
    private String bankName;

    @Column(nullable = false, length = 100)
    private String accountHolderName;

    @Column(nullable = false, length = 20)
    private String bankAccountNumber;

    @Column(nullable = false, length = 11)
    private String bankIfsc;

    protected RiderPrivateDetails() {
    }

    public RiderPrivateDetails(
            String riderId,
            String bankName,
            String accountHolderName,
            String bankAccountNumber,
            String bankIfsc) {

        this.riderId = riderId;
        this.bankName = bankName;
        this.accountHolderName = accountHolderName;
        this.bankAccountNumber = bankAccountNumber;
        this.bankIfsc = bankIfsc;
    }
}