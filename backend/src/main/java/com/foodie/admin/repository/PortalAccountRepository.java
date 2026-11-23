package com.foodie.admin.repository;

import com.foodie.admin.model.PortalAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortalAccountRepository extends JpaRepository<PortalAccount, Long> {
    Optional<PortalAccount> findByEmailIgnoreCase(String email);
    List<PortalAccount> findByRole(String role);
    Optional<PortalAccount> findByProfileId(String profileId);
}
