package com.foodie.admin.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;

import com.foodie.customer.model.User;
import com.foodie.customer.repo.Repos.OrderRepo;
import com.foodie.customer.repo.Repos.RestaurantRepo;
import com.foodie.customer.repo.Repos.UserRepo;
import com.foodie.admin.data.DummyData;
import com.foodie.admin.model.Customer;
import com.foodie.admin.model.DashboardStats;
import com.foodie.admin.model.Detail;
import com.foodie.admin.dto.RiderRequest;
import com.foodie.admin.dto.SellerRequest;
import com.foodie.admin.model.Order;
import com.foodie.admin.model.PortalAccount;
import com.foodie.admin.model.Rider;
import com.foodie.admin.model.RiderDocument;
import com.foodie.admin.model.Seller;
import com.foodie.admin.repository.RiderRepository;
import com.foodie.admin.repository.SellerRepository;
import com.foodie.admin.repository.FoodOrderRepository;
import com.foodie.admin.repository.PortalAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

@Service
public class AdminService {

    private static final Set<String> CUSTOMER_STATUS = Set.of("ACTIVE", "BLOCKED");
    private static final Set<String> SELLER_STATUS = Set.of("ACTIVE", "PENDING", "SUSPENDED");
    private static final Set<String> RIDER_STATUS =
            Set.of("ONLINE", "ON_DELIVERY", "OFFLINE", "PENDING", "REJECTED", "SUSPENDED");

    /** Demo customers/orders are combined with persistent portal accounts and orders. */
    private final DummyData data;
private final SellerRepository sellerRepo;
private final RiderRepository riderRepo;
private final FoodOrderRepository foodOrderRepo;
private final PortalAccountRepository portalAccountRepo;
private final OrderRepo customerOrderRepo;
private final UserRepo customerUserRepo;
private final RestaurantRepo restaurantRepo;

    public AdminService(DummyData data, SellerRepository sellerRepo, RiderRepository riderRepo,
                    FoodOrderRepository foodOrderRepo, PortalAccountRepository portalAccountRepo,
                    OrderRepo customerOrderRepo, UserRepo customerUserRepo,
                    RestaurantRepo restaurantRepo) {
    this.data = data;
    this.sellerRepo = sellerRepo;
    this.riderRepo = riderRepo;
    this.foodOrderRepo = foodOrderRepo;
    this.portalAccountRepo = portalAccountRepo;
    this.customerOrderRepo = customerOrderRepo;
    this.customerUserRepo = customerUserRepo;
    this.restaurantRepo = restaurantRepo;
}

    // ---------------------------------------------------------------- dashboard
    public synchronized DashboardStats dashboard() {
        int ridersOnline = (int) riderRepo.countByStatusIn(List.of("ONLINE", "ON_DELIVERY"));
        int pending = (int) (sellerRepo.countByStatus("PENDING") + riderRepo.countByStatus("PENDING"));
        List<Order> allOrders = allOrders();
        int live = (int) allOrders.stream().filter(o ->
                o.status().equals("ON_THE_WAY") || o.status().equals("PREPARING")
                        || o.status().equals("AWAITING_SELLER")
                        || o.status().equals("AWAITING_RIDER") || o.status().equals("PLACED")
                        || o.status().equals("ACCEPTED") || o.status().equals("READY_FOR_PICKUP")
                        || o.status().equals("RIDER_ASSIGNED") || o.status().equals("PICKED_UP")
                        || o.status().equals("DISPATCHED")).count();
        double revenue = allOrders.stream().filter(o -> o.status().equals("DELIVERED"))
                .mapToDouble(Order::amount).sum();

        return new DashboardStats(customers().size(), (int) sellerRepo.count(), (int) riderRepo.count(),
                ridersOnline, pending, allOrders.size(), live, revenue,
                allOrders.subList(0, Math.min(6, allOrders.size())));
    }

    // ---------------------------------------------------------------- customers
    public synchronized List<Customer> customers() {
        Map<String, Customer> result = new LinkedHashMap<>(data.customers);
        List<Order> allOrders = allOrders();
        customerUserRepo.findAll().forEach(user -> {
            String cid = String.valueOf(user.getId());
            result.putIfAbsent("U-" + cid, customerFromUser(user, allOrders));
        });
        portalAccountRepo.findByRole("CUSTOMER").forEach(account ->
                result.putIfAbsent(account.getProfileId(), customerFromAccount(account, allOrders)));
        return new ArrayList<>(result.values());
    }

    public synchronized Detail<Customer> customer(String id) {
        Customer c = customerById(id);
        String rawId = id.startsWith("U-") ? id.substring(2) : id;
        return new Detail<>(c, ordersWhere(o -> Objects.equals(o.customerId(), id) || Objects.equals(o.customerId(), rawId)));
    }

    public synchronized Customer setCustomerStatus(String id, String status) {
        check(CUSTOMER_STATUS, status);
        if (id.startsWith("U-")) {
            Long uid = Long.parseLong(id.substring(2));
            User user = customerUserRepo.findById(uid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer " + id + " not found"));
            user.setStatus(status);
            customerUserRepo.save(user);
            return customerFromUser(user, allOrders());
        }
        Customer updated = customerById(id).withStatus(status);
        data.customers.put(id, updated);
        portalAccountRepo.findByProfileId(id).ifPresent(account -> {
            account.setStatus(status);
            portalAccountRepo.save(account);
        });
        return updated;
    }

    // ---------------------------------------------------------------- sellers (MySQL)
    public synchronized List<Seller> sellers() {
        return sellerRepo.findAll().stream().sorted(BY_ID_NUMBER_SELLER).toList();
    }

    public synchronized Detail<Seller> seller(String id) {
        Seller s = findSeller(id);
        return new Detail<>(s, ordersWhere(o -> Objects.equals(o.sellerId(), id)));
    }

    public synchronized Seller setSellerStatus(String id, String status, String note) {
    check(SELLER_STATUS, status);

    Seller seller = findSeller(id);

    seller.setStatusNote(
            statusNote(status, seller.getStatus(), note, seller.getStatusNote(), false)
    );
    seller.setStatus(status);

    if (seller.getRestaurantId() != null) {
        boolean shouldBeOpen = "ACTIVE".equalsIgnoreCase(status) && seller.isOpen();
        restaurantRepo.findById(seller.getRestaurantId()).ifPresent(restaurant -> {
            restaurant.setOpen(shouldBeOpen);
            restaurantRepo.save(restaurant);
        });
    }

    portalAccountRepo.findByProfileId(id).ifPresent(account -> {
        account.setStatus(status);
        portalAccountRepo.save(account);
    });

    return sellerRepo.save(seller);
}
    public synchronized Seller createSeller(SellerRequest r) {
        String status = orDefault(r.status(), "ACTIVE");
        check(SELLER_STATUS, status);
        String id = nextId("S", sellerRepo.findAll().stream().map(Seller::getId).toList());
        Seller s = new Seller(id, "", "", null, null, "", "", "", "", "", "", 0.0, status,
                r.open() == null || r.open(), today(), 0, 0.0, List.of());
        s.setStatusNote(statusNote(status, null, r.statusNote(), null, false));
        applySeller(s, r);
        return sellerRepo.save(s);
    }

    public synchronized Seller createSellerFromApplication(
        String name,
        String ownerName,
        String image,
        String cuisine,
        String phone,
        String email,
        String address,
        String city,
        String fssaiNumber) {

    String id = nextId("S", sellerRepo.findAll().stream()
            .map(Seller::getId).toList());

    Seller seller = new Seller(
            id,
            name.trim(),
            ownerName.trim(),
            null,
            blankToNull(image),
            cuisine.trim(),
            phone.trim(),
            email.trim(),
            address.trim(),
            blankToNull(city),
            blankToNull(fssaiNumber),
            0.0,
            "ACTIVE",
            true,
            today(),
            0,
            0.0,
            List.of()
    );

    return sellerRepo.save(seller);
}

    public synchronized Seller updateSeller(String id, SellerRequest r) {
    Seller s = findSeller(id);
    String status = orDefault(r.status(), s.getStatus());
    check(SELLER_STATUS, status);

    s.setStatusNote(
            statusNote(status, s.getStatus(), r.statusNote(), s.getStatusNote(), false)
    );
    s.setStatus(status);

    portalAccountRepo.findByProfileId(id).ifPresent(account -> {
        account.setStatus(status);
        portalAccountRepo.save(account);
    });

    applySeller(s, r);
    if (s.getRestaurantId() != null) {
        restaurantRepo.findById(s.getRestaurantId()).ifPresent(restaurant -> {
            restaurant.setName(s.getName());
            restaurant.setCuisines(s.getCuisine());
            restaurant.setImageUrl(s.getImage());
            if (s.getCity() != null && !s.getCity().isBlank()) {
                restaurant.setArea(s.getCity());
            }
            restaurant.setOpen("ACTIVE".equalsIgnoreCase(s.getStatus()) && s.isOpen());
            restaurantRepo.save(restaurant);
        });
    }
    return sellerRepo.save(s);
}

    public synchronized void deleteSeller(String id) {
        sellerRepo.delete(findSeller(id));
    }

    private void applySeller(Seller s, SellerRequest r) {
        s.setName(r.name().trim());
        s.setOwnerName(r.ownerName().trim());
        s.setOwnerAvatar(blankToNull(r.ownerAvatar()));
        s.setImage(blankToNull(r.image()));
        s.setCuisine(r.cuisine().trim());
        s.setPhone(r.phone().trim());
        s.setEmail(r.email().trim());
        s.setAddress(r.address().trim());
        s.setCity(r.city().trim());
        s.setFssaiNumber(r.fssaiNumber().trim());
        if (r.open() != null) s.setOpen(r.open());
    }

    private Seller findSeller(String id) {
        return find(sellerRepo.findById(id).orElse(null), "Seller", id);
    }

    // ---------------------------------------------------------------- riders (MySQL)
    public synchronized List<Rider> riders() {
        return riderRepo.findAll().stream().sorted(BY_ID_NUMBER_RIDER).toList();
    }

    public synchronized Detail<Rider> rider(String id) {
        Rider r = findRider(id);
        return new Detail<>(r, ordersWhere(o -> id.equals(o.riderId())));
    }

    public synchronized Rider setRiderStatus(String id, String status, String note) {
        check(RIDER_STATUS, status);
        Rider r = findRider(id);
        r.setStatusNote(statusNote(status, r.getStatus(), note, r.getStatusNote(), true));
        r.setStatus(status);
        return riderRepo.save(r);
    }

    public synchronized Rider createRider(RiderRequest r) {
        String status = orDefault(r.status(), "OFFLINE");
        check(RIDER_STATUS, status);
        String initialStatusNote = statusNote(status, null, r.statusNote(), null, true);
        String id = nextId("R", riderRepo.findAll().stream().map(Rider::getId).toList());
        String docStatus = status.equals("PENDING") ? "PENDING" : "VERIFIED";
        List<RiderDocument> docs = List.of(
                new RiderDocument("Driving Licence", "VERIFIED"),
                new RiderDocument("Aadhaar Card", docStatus),
                new RiderDocument("Vehicle RC", docStatus),
                new RiderDocument("Selfie Verification", docStatus));
        Rider rider = new Rider(id, "", null, "", "", "", "", "", "", status, 0.0, 0, 0.0, today(), docs);
        rider.setStatusNote(initialStatusNote);
        applyRider(rider, r);
        return riderRepo.save(rider);
    }

    public synchronized Rider updateRider(String id, RiderRequest r) {
        Rider rider = findRider(id);
        String status = orDefault(r.status(), rider.getStatus());
        check(RIDER_STATUS, status);
        rider.setStatusNote(statusNote(status, rider.getStatus(), r.statusNote(), rider.getStatusNote(), true));
        rider.setStatus(status);
        applyRider(rider, r);
        return riderRepo.save(rider);
    }

    public synchronized void deleteRider(String id) {
        riderRepo.delete(findRider(id));
    }

    private void applyRider(Rider rider, RiderRequest r) {
        rider.setName(r.name().trim());
        rider.setAvatar(blankToNull(r.avatar()));
        rider.setPhone(r.phone().trim());
        rider.setEmail(r.email().trim());
        rider.setVehicle(r.vehicle().trim());
        rider.setVehicleNumber(r.vehicleNumber().trim());
        rider.setLicenseNumber(r.licenseNumber().trim());
        rider.setCity(r.city().trim());
    }

    private Rider findRider(String id) {
        return find(riderRepo.findById(id).orElse(null), "Rider", id);
    }

    // ---------------------------------------------------------------- orders
    public synchronized List<Order> orders() {
        return allOrders();
    }

    // ---------------------------------------------------------------- helpers
    private List<Order> ordersWhere(java.util.function.Predicate<Order> p) {
        return allOrders().stream().filter(p).toList();
    }

    
private List<Order> allOrders() {
    Map<String, Order> combined = new LinkedHashMap<>();
    Map<String, Instant> orderTimes = new LinkedHashMap<>();

    // Existing Admin orders
    foodOrderRepo.findAll().forEach(order -> {
        combined.put(order.getId(), order.toOrder());
        orderTimes.put(order.getId(), order.getCreatedAt());
    });

    // Customer portal orders
    customerOrderRepo.findAll().forEach(order -> {
        String id = "C-" + order.getId();
        String customerId = String.valueOf(order.getUserId());

        String customerName = customerUserRepo.findById(order.getUserId())
                .map(user -> user.getName())
                .orElse("Customer #" + customerId);

        String riderName = order.getRiderId() == null ? null :
                riderRepo.findById(order.getRiderId())
                        .map(Rider::getName)
                        .orElse(null);

        String items = order.getItems().stream()
                .map(item -> item.getQty() + "x " + item.getName())
                .collect(java.util.stream.Collectors.joining(", "));

        Instant createdAt = order.getCreatedAt();

        String placedAt = createdAt == null ? "" :
                DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)
                        .withZone(ZoneId.systemDefault())
                        .format(createdAt);

        String status = switch (order.getStatus()) {
            case PLACED -> "PLACED";
            case CONFIRMED -> "ACCEPTED";
            case PREPARING -> "PREPARING";
            case OUT_FOR_DELIVERY -> "ON_THE_WAY";
            case DELIVERED -> "DELIVERED";
            case CANCELLED -> "CANCELLED";
        };

        String sellerId = sellerRepo.findByRestaurantId(order.getRestaurantId())
                .map(Seller::getId)
                .orElse(null);

        Order adminOrder = new Order(
                id,
                customerId,
                customerName,
                sellerId,
                order.getRestaurantName(),
                order.getRiderId(),
                riderName,
                items,
                order.getTotal(),
                status,
                order.getPaymentMethod(),
                order.getAddress(),
                placedAt
        );

        combined.put(id, adminOrder);
        orderTimes.put(id, createdAt);
    });

    // Existing demo orders
    data.orders.forEach(order -> combined.putIfAbsent(order.id(), order));

    // Newest orders first
    List<Order> result = new ArrayList<>(combined.values());

    result.sort(Comparator.comparing(
            (Order order) -> orderTimes.get(order.id()),
            Comparator.nullsLast(Comparator.reverseOrder())
    ));

    return result;
}


    private Customer customerById(String id) {
        Customer customer = data.customers.get(id);
        if (customer != null) return customer;
        if (id.startsWith("U-")) {
            try {
                Long uid = Long.parseLong(id.substring(2));
                User user = customerUserRepo.findById(uid).orElse(null);
                if (user != null) return customerFromUser(user, allOrders());
            } catch (NumberFormatException ignored) {
                // Fall through to portalAccount lookup / 404.
            }
        }
        PortalAccount account = portalAccountRepo.findByProfileId(id).orElse(null);
        return find(account == null ? null : customerFromAccount(account, allOrders()), "Customer", id);
    }

    private Customer customerFromUser(User user, List<Order> allOrders) {
        String rawId = String.valueOf(user.getId());
        String displayId = "U-" + rawId;
        List<Order> customerOrders = allOrders.stream()
                .filter(order -> Objects.equals(order.customerId(), rawId) || Objects.equals(order.customerId(), displayId))
                .toList();
        double spent = customerOrders.stream()
                .filter(order -> "DELIVERED".equals(order.status()))
                .mapToDouble(Order::amount)
                .sum();
        String joined = user.getCreatedAt() == null
                ? today()
                : DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
                        .withZone(ZoneId.systemDefault())
                        .format(user.getCreatedAt());
        String area = user.getLocationLabel() == null || user.getLocationLabel().isBlank()
                ? "Visakhapatnam"
                : user.getLocationLabel();
        return new Customer(
                displayId,
                user.getName(),
                user.getEmail(),
                user.getPhone() == null ? "" : user.getPhone(),
                null,
                area,
                area,
                joined,
                user.getStatus(),
                customerOrders.size(),
                spent
        );
    }

    private Customer customerFromAccount(PortalAccount account, List<Order> allOrders) {
        List<Order> customerOrders = allOrders.stream().filter(order ->
                Objects.equals(order.customerId(), account.getProfileId())).toList();
        double spent = customerOrders.stream().filter(order -> "DELIVERED".equals(order.status()))
                .mapToDouble(Order::amount).sum();
        return new Customer(account.getProfileId(), account.getDisplayName(), account.getEmail(),
                account.getPhone(), null, "Visakhapatnam", "", today(), account.getStatus(),
                customerOrders.size(), spent);
    }

    private static <T> T find(T value, String what, String id) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, what + " " + id + " not found");
        }
        return value;
    }

    private static final java.util.Comparator<Seller> BY_ID_NUMBER_SELLER =
            java.util.Comparator.comparingInt(x -> idNumber(x.getId()));
    private static final java.util.Comparator<Rider> BY_ID_NUMBER_RIDER =
            java.util.Comparator.comparingInt(x -> idNumber(x.getId()));

    /** "S12" -> 12, so S10 sorts after S9. */
    private static int idNumber(String id) {
        try {
            return Integer.parseInt(id.substring(1));
        } catch (RuntimeException e) {
            return Integer.MAX_VALUE;
        }
    }

    /** Next free id such as S7 / R7: highest existing number + 1. */
    private static String nextId(String prefix, Collection<String> existing) {
        int max = 0;
        for (String id : existing) {
            int n = idNumber(id);
            if (n != Integer.MAX_VALUE && n > max) max = n;
        }
        return prefix + (max + 1);
    }

    private static String today() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void check(Set<String> allowed, String status) {
        if (!allowed.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status '" + status + "'. Allowed: " + allowed);
        }
    }

    private static String statusNote(String status, String previousStatus, String note, String existingNote,
                                     boolean rejectedNeedsNote) {
        boolean noteRequired = "SUSPENDED".equals(status)
                || (rejectedNeedsNote && "REJECTED".equals(status));
        if (!noteRequired) return null;
        if (note != null && !note.isBlank()) return note.trim();
        if (status.equals(previousStatus) && existingNote != null && !existingNote.isBlank()) {
            return existingNote;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A reason note is required for this status");
    }
}
