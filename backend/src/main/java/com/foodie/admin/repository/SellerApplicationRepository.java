package com.foodie.admin.repository;

import com.foodie.admin.model.SellerApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerApplicationRepository extends JpaRepository<SellerApplication, String> {
    Optional<SellerApplication> findByEmailIgnoreCase(String email);
    List<SellerApplication> findAllByOrderBySubmittedAtDesc();
}
