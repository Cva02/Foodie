package com.foodie.admin.model;

import jakarta.persistence.Embeddable;

/** One dish on a seller's menu (MySQL table `seller_menu_items`). */
@Embeddable
public record MenuItem(String name, String category, double price, boolean veg, String image) {
}
