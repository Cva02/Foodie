package com.foodie.admin.repository;

import com.foodie.admin.model.RiderSupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiderSupportTicketRepository
        extends JpaRepository<RiderSupportTicket, Long> {

    List<RiderSupportTicket> findByRiderIdOrderByCreatedAtDesc(String riderId);
}