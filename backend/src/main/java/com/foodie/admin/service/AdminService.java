package com.foodie.admin.service;

import com.foodie.admin.data.DummyData;
import com.foodie.admin.model.Customer;
import com.foodie.admin.model.CustomerProfileUpdate;
import com.foodie.admin.model.DashboardStats;
import com.foodie.admin.model.Detail;
import com.foodie.admin.model.Order;
import com.foodie.admin.model.Rider;
import com.foodie.admin.model.RiderProfileUpdate;
import com.foodie.admin.model.Seller;
import com.foodie.admin.model.SellerProfileUpdate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class AdminService {

    private static final Set<String> CUSTOMER_STATUS = Set.of("ACTIVE", "BLOCKED");
    private static final Set<String> SELLER_STATUS = Set.of("ACTIVE", "PENDING", "SUSPENDED");
    private static final Set<String> RIDER_STATUS =
            Set.of("ONLINE", "ON_DELIVERY", "OFFLINE", "PENDING", "REJECTED", "SUSPENDED");

    private final DummyData data;

    public AdminService(DummyData data) {
        this.data = data;
    }

    // ---------------------------------------------------------------- dashboard
    public synchronized DashboardStats dashboard() {
        int ridersOnline = (int) data.riders.values().stream()
                .filter(r -> r.status().equals("ONLINE") || r.status().equals("ON_DELIVERY")).count();
        int pending = (int) (data.sellers.values().stream().filter(s -> s.status().equals("PENDING")).count()
                + data.riders.values().stream().filter(r -> r.status().equals("PENDING")).count());
        int live = (int) data.orders.stream().filter(o ->
                o.status().equals("ON_THE_WAY") || o.status().equals("PREPARING")
                        || o.status().equals("AWAITING_RIDER")).count();
        double revenue = data.orders.stream().filter(o -> o.status().equals("DELIVERED"))
                .mapToDouble(Order::amount).sum();

        return new DashboardStats(data.customers.size(), data.sellers.size(), data.riders.size(),
                ridersOnline, pending, data.orders.size(), live, revenue,
                data.orders.subList(0, Math.min(6, data.orders.size())));
    }

    // ---------------------------------------------------------------- customers
    public synchronized List<Customer> customers() {
        return new ArrayList<>(data.customers.values());
    }

    public synchronized Detail<Customer> customer(String id) {
        Customer c = find(data.customers.get(id), "Customer", id);
        return new Detail<>(c, ordersWhere(o -> o.customerId().equals(id)));
    }

    public synchronized Customer setCustomerStatus(String id, String status) {
        check(CUSTOMER_STATUS, status);
        Customer updated = find(data.customers.get(id), "Customer", id).withStatus(status);
        data.customers.put(id, updated);
        return updated;
    }

    public synchronized Customer updateCustomerProfile(String id, CustomerProfileUpdate profile) {
        requireProfile(profile.name(), "Name");
        requireEmail(profile.email());
        requireProfile(profile.phone(), "Phone");
        requireProfile(profile.city(), "City");
        requireProfile(profile.address(), "Address");
        Customer updated = find(data.customers.get(id), "Customer", id).withProfile(profile);
        data.customers.put(id, updated);
        return updated;
    }

    // ---------------------------------------------------------------- sellers
    public synchronized List<Seller> sellers() {
        return new ArrayList<>(data.sellers.values());
    }

    public synchronized Detail<Seller> seller(String id) {
        Seller s = find(data.sellers.get(id), "Seller", id);
        return new Detail<>(s, ordersWhere(o -> o.sellerId().equals(id)));
    }

    public synchronized Seller setSellerStatus(String id, String status) {
        check(SELLER_STATUS, status);
        Seller updated = find(data.sellers.get(id), "Seller", id).withStatus(status);
        data.sellers.put(id, updated);
        return updated;
    }

    public synchronized Seller updateSellerProfile(String id, SellerProfileUpdate profile) {
        requireProfile(profile.name(), "Restaurant name");
        requireProfile(profile.ownerName(), "Owner name");
        requireProfile(profile.phone(), "Phone");
        requireEmail(profile.email());
        requireProfile(profile.cuisine(), "Cuisine");
        requireProfile(profile.address(), "Address");
        requireProfile(profile.city(), "City");
        requireProfile(profile.fssaiNumber(), "FSSAI licence");
        Seller updated = find(data.sellers.get(id), "Seller", id).withProfile(profile);
        data.sellers.put(id, updated);
        return updated;
    }

    // ---------------------------------------------------------------- riders
    public synchronized List<Rider> riders() {
        return new ArrayList<>(data.riders.values());
    }

    public synchronized Detail<Rider> rider(String id) {
        Rider r = find(data.riders.get(id), "Rider", id);
        return new Detail<>(r, ordersWhere(o -> id.equals(o.riderId())));
    }

    public synchronized Rider setRiderStatus(String id, String status) {
        check(RIDER_STATUS, status);
        Rider updated = find(data.riders.get(id), "Rider", id).withStatus(status);
        data.riders.put(id, updated);
        return updated;
    }

    public synchronized Rider updateRiderProfile(String id, RiderProfileUpdate profile) {
        requireProfile(profile.name(), "Name");
        requireProfile(profile.phone(), "Phone");
        requireEmail(profile.email());
        requireProfile(profile.city(), "Service area");
        requireProfile(profile.vehicle(), "Vehicle");
        requireProfile(profile.vehicleNumber(), "Vehicle registration");
        requireProfile(profile.licenseNumber(), "Driving licence");
        Rider updated = find(data.riders.get(id), "Rider", id).withProfile(profile);
        data.riders.put(id, updated);
        return updated;
    }

    // ---------------------------------------------------------------- helpers
    private List<Order> ordersWhere(java.util.function.Predicate<Order> p) {
        return data.orders.stream().filter(p).toList();
    }

    private static <T> T find(T value, String what, String id) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, what + " " + id + " not found");
        }
        return value;
    }

    private static void check(Set<String> allowed, String status) {
        if (!allowed.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status '" + status + "'. Allowed: " + allowed);
        }
    }

    private static void requireProfile(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " is required");
        }
    }

    private static void requireEmail(String email) {
        requireProfile(email, "Email");
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address");
        }
    }
}
