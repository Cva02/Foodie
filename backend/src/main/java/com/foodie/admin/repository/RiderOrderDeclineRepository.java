package com.foodie.admin.repository;

import com.foodie.admin.model.RiderOrderDecline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiderOrderDeclineRepository
        extends JpaRepository<RiderOrderDecline, Long> {

    boolean existsByRiderIdAndOrderId(String riderId, Long orderId);

    Optional<RiderOrderDecline> findByRiderIdAndOrderId(
            String riderId, Long orderId
    );

    List<RiderOrderDecline> findByRiderId(String riderId);
}
