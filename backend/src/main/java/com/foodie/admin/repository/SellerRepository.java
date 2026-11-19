package com.foodie.admin.repository;

import com.foodie.admin.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, String> {
    long countByStatus(String status);
    boolean existsByEmailIgnoreCase(String email);
}
