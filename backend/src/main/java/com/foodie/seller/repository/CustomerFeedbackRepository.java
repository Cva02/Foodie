package com.foodie.seller.repository;

import com.foodie.seller.entity.CustomerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {
    List<CustomerFeedback> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}
