package com.foodie.admin.repository;

import com.foodie.admin.model.Rider;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RiderRepository extends JpaRepository<Rider, String> {
    boolean existsByEmailIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Rider r where r.id = :id")
    Optional<Rider> findLockedById(@Param("id") String id);
}
