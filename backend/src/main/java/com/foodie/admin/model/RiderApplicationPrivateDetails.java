package com.foodie.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rider_application_private_details")
public class RiderApplicationPrivateDetails {

    @Id
    @Column(length = 36)
    private String applicationId;

    @Column(nullable = false, length = 100)
    private String bankName;

    @Column(nullable = false, length = 100)
    private String accountHolderName;

    @Column(nullable = false, length = 20)
    private String bankAccountNumber;

    @Column(nullable = false, length = 11)
    private String bankIfsc;

    protected RiderApplicationPrivateDetails() {
    }

    public RiderApplicationPrivateDetails(
            String applicationId,
            String bankName,
            String accountHolderName,
            String bankAccountNumber,
            String bankIfsc) {

        this.applicationId = applicationId;
        this.bankName = bankName;
        this.accountHolderName = accountHolderName;
        this.bankAccountNumber = bankAccountNumber;
        this.bankIfsc = bankIfsc;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public String getBankIfsc() {
        return bankIfsc;
    }
}