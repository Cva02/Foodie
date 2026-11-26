package com.foodie.admin.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class FoodOrderItem {

    private String name;
    private double price;
    private int quantity;

    protected FoodOrderItem() {
    }

    public FoodOrderItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
}
