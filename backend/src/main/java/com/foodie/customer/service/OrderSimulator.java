
package com.foodie.customer.service;

import com.foodie.admin.model.Rider;
import com.foodie.admin.repository.RiderRepository;
import com.foodie.customer.model.OrderEntity;
import com.foodie.customer.model.OrderStatus;
import com.foodie.customer.repo.Repos.OrderRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "foodie.simulation.enabled", havingValue = "true")
public class OrderSimulator {
    private final OrderRepo orders;
    private final RiderRepository riders;
    private final NotificationService notifications;
    private final long[] steps;
    private final Random random = new Random();

    public OrderSimulator(OrderRepo orders, RiderRepository riders,
                          NotificationService notifications,
                          @Value("${foodie.simulation.step-seconds}") String stepSeconds) {
        this.orders = orders;
        this.riders = riders;
        this.notifications = notifications;
        String[] p = stepSeconds.split(",");
        this.steps = new long[p.length];
        for (int i = 0; i < p.length; i++) {
            steps[i] = Long.parseLong(p[i].trim());
        }
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void tick() {
        List<OrderEntity> active = orders.findByStatusIn(Set.of(
                OrderStatus.PLACED, OrderStatus.CONFIRMED,
                OrderStatus.PREPARING, OrderStatus.OUT_FOR_DELIVERY));

        Instant now = Instant.now();

        for (OrderEntity o : active) {
            OrderStatus s = o.getStatus();
            long wait = steps[Math.min(s.ordinal(), steps.length - 1)];

            if (Duration.between(o.getStatusUpdatedAt(), now).getSeconds() < wait) {
                continue;
            }

            switch (s) {
                case PLACED -> {
                    o.setStatusNow(OrderStatus.CONFIRMED);
                    assignRider(o);
                    notifications.toUser(o.getUserId(), "ORDER_CONFIRMED",
                            "Order confirmed",
                            o.getRestaurantName() + " accepted your order #" + o.getId() + ".",
                            o.getId());
                }
                case CONFIRMED -> {
                    o.setStatusNow(OrderStatus.PREPARING);
                    notifications.toUser(o.getUserId(), "ORDER_PREPARING",
                            "Preparing your food",
                            "The kitchen has started preparing order #" + o.getId() + ".",
                            o.getId());
                }
                case PREPARING -> {
                    o.setStatusNow(OrderStatus.OUT_FOR_DELIVERY);
                    String rider = o.getRiderId() == null ? "Your rider"
                            : riders.findById(o.getRiderId())
                                    .map(Rider::getName).orElse("Your rider");
                    notifications.toUser(o.getUserId(), "ORDER_DISPATCHED",
                            "Order dispatched",
                            rider + " has picked up order #" + o.getId() + " and is on the way.",
                            o.getId());
                }
                case OUT_FOR_DELIVERY -> {
                    o.setStatusNow(OrderStatus.DELIVERED);
                    notifications.toUser(o.getUserId(), "ORDER_DELIVERED",
                            "Order delivered",
                            "Order #" + o.getId() + " has been delivered. Enjoy your meal!",
                            o.getId());
                }
                default -> { }
            }
        }
    }

    private void assignRider(OrderEntity o) {
        List<Rider> available = riders.findAll().stream()
                .filter(r -> "ONLINE".equalsIgnoreCase(r.getStatus()))
                .toList();

        if (!available.isEmpty()) {
            o.setRiderId(available.get(random.nextInt(available.size())).getId());
        }
    }
}
