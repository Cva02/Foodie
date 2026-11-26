package com.foodie.admin.repository;

import com.foodie.admin.model.RiderApplicationPrivateDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiderApplicationPrivateDetailsRepository
        extends JpaRepository<RiderApplicationPrivateDetails, String> {
}