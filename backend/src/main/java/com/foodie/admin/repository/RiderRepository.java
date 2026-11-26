package com.foodie.admin.repository;

import com.foodie.admin.model.Rider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface RiderRepository extends JpaRepository<Rider, String> {
    long countByStatus(String status);
    long countByStatusIn(Collection<String> statuses);
    boolean existsByEmailIgnoreCase(String email);
}
