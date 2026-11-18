package com.foodie.admin.model;

import java.util.List;

/** Profile + that profile's orders, returned by the "click on one" endpoints. */
public record Detail<T>(T profile, List<Order> orders) {
}
