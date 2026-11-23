package com.foodie.admin.repository;

import com.foodie.admin.model.RiderNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiderNotificationRepository extends JpaRepository<RiderNotification, Long> {
    List<RiderNotification> findTop50ByRiderIdAndReadAtIsNullOrderByCreatedAtDesc(String riderId);
    Optional<RiderNotification> findByIdAndRiderId(Long id, String riderId);
}
