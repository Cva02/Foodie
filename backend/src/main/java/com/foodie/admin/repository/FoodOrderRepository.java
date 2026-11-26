package com.foodie.admin.repository;

import com.foodie.admin.model.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, String> {
    List<FoodOrder> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<FoodOrder> findBySellerIdOrderByCreatedAtDesc(String sellerId);
    List<FoodOrder> findByStatusInOrderByCreatedAtAsc(List<String> statuses);
    List<FoodOrder> findByRiderIdOrderByCreatedAtDesc(String riderId);
}
