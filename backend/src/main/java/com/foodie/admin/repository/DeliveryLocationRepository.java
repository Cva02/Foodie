package com.foodie.admin.repository;

import com.foodie.admin.model.DeliveryLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryLocationRepository extends JpaRepository<DeliveryLocation, Long> {
    List<DeliveryLocation> findByOrderIdOrderByRecordedAtAsc(String orderId);
}
