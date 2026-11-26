package com.foodie.admin.repository;

import com.foodie.admin.model.RiderApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiderApplicationRepository extends JpaRepository<RiderApplication, String> {
    Optional<RiderApplication> findByEmailIgnoreCase(String email);
    List<RiderApplication> findAllByOrderBySubmittedAtDesc();
}
