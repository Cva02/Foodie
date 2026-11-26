package com.foodie.admin.repository;

import com.foodie.admin.model.RiderPrivateDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiderPrivateDetailsRepository
        extends JpaRepository<RiderPrivateDetails, String> {
}