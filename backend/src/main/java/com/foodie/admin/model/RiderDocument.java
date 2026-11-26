package com.foodie.admin.model;

import jakarta.persistence.Embeddable;

/** One verification document of a rider (MySQL table `rider_documents`). */
@Embeddable
public record RiderDocument(String type, String status) {   // VERIFIED | PENDING | REJECTED
}
