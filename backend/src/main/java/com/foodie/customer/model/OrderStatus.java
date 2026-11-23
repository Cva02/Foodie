package com.foodie.customer.model;

public enum OrderStatus {
    PLACED, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED;

    /** Customer may cancel only before the restaurant starts preparing. */
    public boolean cancellable() {
        return this == PLACED || this == CONFIRMED;
    }

    public boolean active() {
        return this != DELIVERED && this != CANCELLED;
    }
}
