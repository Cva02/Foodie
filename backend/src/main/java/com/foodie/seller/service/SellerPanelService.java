package com.foodie.seller.service;

import com.foodie.seller.dto.Dtos.*;
import com.foodie.seller.entity.*;
import com.foodie.seller.exception.ApiException;
import com.foodie.seller.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;

import static com.foodie.seller.entity.OrderStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class SellerPanelService {

    /** Allowed order status transitions. Anything else is rejected with 409. */
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            PENDING, Set.of(ACCEPTED, REJECTED),
            ACCEPTED, Set.of(PREPARING, CANCELLED),
            PREPARING, Set.of(READY_FOR_PICKUP),
            READY_FOR_PICKUP, Set.of(DELIVERED));

    private final SellerRepository sellers;
    private final MenuItemRepository menu;
    private final OrderRepository orders;
    private final PayoutRepository payouts;
    private final CustomerFeedbackRepository feedback;
    private final PasswordEncoder encoder;

    private static final Map<String, OtpEntry> OTPs = new ConcurrentHashMap<>();
    private record OtpEntry(String code, long expiresAt) {}

    // =====================================================================
    // Auth
    // =====================================================================

    public SessionResponse login(LoginRequest req) {
        String id = req.identifier().trim();
        Optional<Seller> found = id.contains("@") ? sellers.findByEmailIgnoreCase(id) : sellers.findByPhone(id);
        Seller seller = found
                .filter(s -> encoder.matches(req.password(), s.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid mobile/email or password"));
        return toSession(seller);
    }

    public ForgotPasswordResponse requestPasswordOtp(ForgotPasswordRequest req) {
        String id = req.identifier().trim();
        Optional<Seller> found = id.contains("@")
                ? sellers.findByEmailIgnoreCase(id)
                : sellers.findByPhone(id);
        Seller seller = found.orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "No account found for this email or mobile number"));

        String otp = "%06d".formatted(ThreadLocalRandom.current().nextInt(0, 1000000));
        OTPs.put(normalizeIdentifier(id), new OtpEntry(otp, System.currentTimeMillis() + 10 * 60 * 1000));

        boolean email = id.contains("@");
        String masked = email
                ? maskEmail(seller.getEmail())
                : maskPhone(seller.getPhone());
        return new ForgotPasswordResponse(
                "OTP sent successfully",
                email ? "email" : "sms",
                masked);
    }

    public void verifyPasswordOtp(VerifyOtpRequest req) {
        validateOtp(req.identifier(), req.otp());
    }

    public void resetPassword(ResetPasswordRequest req) {
        validateOtp(req.identifier(), req.otp());
        String id = req.identifier().trim();
        Optional<Seller> found = id.contains("@")
                ? sellers.findByEmailIgnoreCase(id)
                : sellers.findByPhone(id);
        Seller seller = found.orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "No account found for this email or mobile number"));
        seller.setPasswordHash(encoder.encode(req.password()));
        sellers.save(seller);
        OTPs.remove(normalizeIdentifier(id));
    }

    private void validateOtp(String identifier, String otp) {
        OtpEntry entry = OTPs.get(normalizeIdentifier(identifier));
        if (entry == null || entry.expiresAt() < System.currentTimeMillis()) {
            OTPs.remove(normalizeIdentifier(identifier));
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP expired or not requested");
        }
        if (!entry.code().equals(otp)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }
    }

    private String normalizeIdentifier(String identifier) {
        String id = identifier.trim();
        return id.contains("@") ? id.toLowerCase(Locale.ROOT) : id;
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(at);
        return email.substring(0, 1) + "***" + email.substring(at);
    }

    private String maskPhone(String phone) {
        if (phone.length() <= 4) return "****";
        return "******" + phone.substring(phone.length() - 4);
    }

    public SessionResponse register(RegisterRequest req) {
        if (sellers.existsByEmailIgnoreCase(req.email().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "This email is already registered");
        }
        if (sellers.existsByPhone(req.phone())) {
            throw new ApiException(HttpStatus.CONFLICT, "This mobile number is already registered");
        }
        Seller seller = Seller.builder()
                .restaurantName(req.restaurantName().trim())
                .ownerName(req.ownerName().trim())
                .email(req.email().trim().toLowerCase())
                .phone(req.phone())
                .passwordHash(encoder.encode(req.password()))
                .cuisine(req.cuisine() == null ? null : req.cuisine().trim())
                .address(req.address() == null ? null : req.address().trim())
                .foodLicenseCertificateUrl(req.foodLicenseCertificateUrl())
                .gstinCertificateUrl(req.gstinCertificateUrl())
                .bankAccountNumber(req.bankAccountNumber().trim())
                .bankIfscCode(req.bankIfscCode().trim().toUpperCase())
                .bankAccountHolderName(req.bankAccountHolderName().trim())
                .online(false)
                .build();
        sellers.save(seller);
        seller.setStoreId("FD-" + (1000 + seller.getId()));
        return toSession(seller);
    }

    private SessionResponse toSession(Seller s) {
        return new SessionResponse(s.getId(), s.getRestaurantName(), s.getStoreId(), s.isOnline());
    }

    // =====================================================================
    // Dashboard KPIs
    // =====================================================================

    @Transactional(readOnly = true)
    public SummaryResponse summary(Long sellerId) {
        requireSeller(sellerId);
        BigDecimal total = orZero(orders.sumTotal(sellerId, DELIVERED, true));      // settled / earned
        BigDecimal pending = orZero(orders.sumTotal(sellerId, DELIVERED, false));   // awaiting payout
        long active = orders.countBySellerIdAndStatusIn(sellerId, OrderStatus.ACTIVE);
        long pendingOrders = orders.countBySellerIdAndStatus(sellerId, PENDING);
        long totalOrders = orders.countBySellerId(sellerId);
        return new SummaryResponse(total, pending, active, pendingOrders, totalOrders);
    }

    @Transactional(readOnly = true)
    public List<CustomerFeedback> feedback(Long sellerId) {
        requireSeller(sellerId);
        return feedback.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    // =====================================================================
    // Orders
    // =====================================================================

    /** group: pending | active | queue (pending + active) | history | all */
    @Transactional(readOnly = true)
    public List<OrderEntity> listOrders(Long sellerId, String group) {
        requireSeller(sellerId);
        Collection<OrderStatus> statuses;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        switch (group.toLowerCase()) {
            case "pending" -> {
                statuses = EnumSet.of(PENDING);
                sort = Sort.by(Sort.Direction.ASC, "createdAt");
            }
            case "active" -> {
                statuses = OrderStatus.ACTIVE;
                sort = Sort.by(Sort.Direction.ASC, "createdAt");
            }
            case "queue" -> {
                EnumSet<OrderStatus> queue = EnumSet.copyOf(OrderStatus.ACTIVE);
                queue.add(PENDING);
                statuses = queue;
                sort = Sort.by(Sort.Direction.ASC, "createdAt");
            }
            case "history" -> statuses = OrderStatus.HISTORY;
            case "all" -> statuses = EnumSet.allOf(OrderStatus.class);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown order group: " + group);
        }
        return orders.findBySellerIdAndStatusIn(sellerId, statuses, PageRequest.of(0, 200, sort));
    }

    public OrderEntity updateOrderStatus(Long sellerId, Long orderId, OrderStatus next) {
        requireSeller(sellerId);
        OrderEntity order = orders.findByIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        Set<OrderStatus> allowed = TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(next)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot move order from %s to %s".formatted(order.getStatus(), next));
        }
        if (next == READY_FOR_PICKUP && order.getPickupCode() == null) {
            order.setPickupCode("%04d".formatted(ThreadLocalRandom.current().nextInt(1000, 10000)));
        }
        order.setStatus(next);
        return orders.save(order);
    }

    /** Creates an order row for order ingestion. */
    public OrderEntity placeOrder(Long sellerId, String customer, String paymentMode, List<OrderLine> lines,
                                  OrderStatus status, boolean settled, LocalDateTime createdAt) {
        Seller seller = requireSeller(sellerId);
        BigDecimal itemTotal = lines.stream()
                .map(l -> l.getPrice().multiply(BigDecimal.valueOf(l.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal packaging = seller.getPackagingCharge();
        OrderEntity order = OrderEntity.builder()
                .sellerId(sellerId)
                .customerName(customer)
                .paymentMode(paymentMode)
                .items(new ArrayList<>(lines))
                .itemTotal(itemTotal)
                .packagingCharge(packaging)
                .total(itemTotal.add(packaging))
                .status(status)
                .settled(settled)
                .createdAt(createdAt)
                .build();
        if (status == READY_FOR_PICKUP) {
            order.setPickupCode("%04d".formatted(ThreadLocalRandom.current().nextInt(1000, 10000)));
        }
        orders.save(order);
        order.setOrderNumber("FD" + (10000 + order.getId()));
        return order;
    }

    // =====================================================================
    // Menu CRUD
    // =====================================================================

    @Transactional(readOnly = true)
    public List<MenuItem> menu(Long sellerId) {
        requireSeller(sellerId);
        return menu.findBySellerIdOrderByCategoryAscNameAsc(sellerId);
    }

    public MenuItem addMenuItem(Long sellerId, MenuItemRequest req) {
        requireSeller(sellerId);
        MenuItem item = new MenuItem();
        item.setSellerId(sellerId);
        apply(item, req);
        return menu.save(item);
    }

    public MenuItem updateMenuItem(Long sellerId, Long itemId, MenuItemRequest req) {
        requireSeller(sellerId);
        MenuItem item = findItem(sellerId, itemId);
        apply(item, req);
        return item;
    }

    public MenuItem setStock(Long sellerId, Long itemId, boolean inStock) {
        requireSeller(sellerId);
        MenuItem item = findItem(sellerId, itemId);
        item.setInStock(inStock);
        return item;
    }

    public void deleteMenuItem(Long sellerId, Long itemId) {
        requireSeller(sellerId);
        menu.delete(findItem(sellerId, itemId));
    }

    private MenuItem findItem(Long sellerId, Long itemId) {
        return menu.findByIdAndSellerId(itemId, sellerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Dish not found"));
    }

    private void apply(MenuItem item, MenuItemRequest req) {
        String image = req.imageUrl() == null ? null : req.imageUrl().trim();
        if (image != null && !image.isEmpty() && !image.startsWith("http://") && !image.startsWith("https://")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Image URL must start with http:// or https://");
        }
        item.setName(req.name().trim());
        item.setDescription(req.description() == null ? null : req.description().trim());
        item.setPrice(req.price());
        item.setCategory(req.category().trim());
        item.setVeg(req.veg());
        item.setInStock(req.inStock() == null || req.inStock());
        item.setImageUrl(image == null || image.isEmpty() ? null : image);
        item.setCustomizationOptions(req.customizationOptions() == null ? null : req.customizationOptions().trim());
    }

    // =====================================================================
    // Earnings & payouts
    // =====================================================================

    @Transactional(readOnly = true)
    public EarningsResponse earnings(Long sellerId) {
        requireSeller(sellerId);
        return new EarningsResponse(
                orZero(orders.sumTotal(sellerId, DELIVERED, true)),
                orZero(orders.sumTotal(sellerId, DELIVERED, false)),
                payouts.findBySellerIdOrderByPaidAtDesc(sellerId));
    }

    /** Settles every delivered-but-unsettled order into a new payout. */
    public EarningsResponse requestPayout(Long sellerId) {
        requireSeller(sellerId);
        List<OrderEntity> due = orders.findBySellerIdAndStatusAndSettled(sellerId, DELIVERED, false);
        if (due.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No pending amount to settle");
        }
        BigDecimal amount = due.stream().map(OrderEntity::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        due.forEach(o -> o.setSettled(true));
        payouts.save(Payout.builder()
                .sellerId(sellerId)
                .amount(amount)
                .orderCount(due.size())
                .status("PROCESSED")
                .reference("PO" + System.currentTimeMillis())
                .paidAt(LocalDateTime.now())
                .build());
        return earnings(sellerId); // JPQL query auto-flushes the changes above
    }

    // =====================================================================
    // Profile & store status
    // =====================================================================

    @Transactional(readOnly = true)
    public Seller profile(Long sellerId) {
        return requireSeller(sellerId);
    }

    public Seller updateProfile(Long sellerId, ProfileRequest req) {
        Seller s = requireSeller(sellerId);
        if (!s.getPhone().equals(req.phone()) && sellers.existsByPhone(req.phone())) {
            throw new ApiException(HttpStatus.CONFLICT, "This mobile number is already registered");
        }
        if (req.email() != null) {
            String email = req.email().trim().toLowerCase();
            if (!s.getEmail().equalsIgnoreCase(email) && sellers.existsByEmailIgnoreCase(email)) {
                throw new ApiException(HttpStatus.CONFLICT, "This email is already registered");
            }
        }
        s.setRestaurantName(req.restaurantName().trim());
        s.setOwnerName(req.ownerName());
        s.setAddress(req.address());
        s.setCuisine(req.cuisine());
        if (req.openingTime() != null) s.setOpeningTime(req.openingTime());
        if (req.closingTime() != null) s.setClosingTime(req.closingTime());
        if (req.packagingCharge() != null) s.setPackagingCharge(req.packagingCharge());
        s.setPhone(req.phone());
        if (req.email() != null && !req.email().trim().isEmpty()) s.setEmail(req.email().trim().toLowerCase());
        if (req.bankAccountNumber() != null) s.setBankAccountNumber(req.bankAccountNumber().trim());
        if (req.bankIfscCode() != null) s.setBankIfscCode(req.bankIfscCode().trim().toUpperCase());
        if (req.bankAccountHolderName() != null) s.setBankAccountHolderName(req.bankAccountHolderName().trim());
        return s;
    }

    public SessionResponse setOnline(Long sellerId, boolean online) {
        Seller s = requireSeller(sellerId);
        s.setOnline(online);
        return toSession(s);
    }

    // =====================================================================

    private Seller requireSeller(Long id) {
        return sellers.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Seller not found"));
    }

    private static BigDecimal orZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
