package com.foodie.admin.repository;

import com.foodie.admin.model.RiderDeclinedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiderDeclinedOrderRepository extends JpaRepository<RiderDeclinedOrder, Long> {
    boolean existsByRiderIdAndOrderId(String riderId, String orderId);
    List<RiderDeclinedOrder> findByRiderId(String riderId);
}
