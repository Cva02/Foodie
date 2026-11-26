package com.foodie.customer.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import com.foodie.customer.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public final class Repos {
    private Repos() {}

    public interface UserRepo extends JpaRepository<User, Long> {
        Optional<User> findByEmailIgnoreCase(String email);
        boolean existsByEmailIgnoreCase(String email);
    }
    public interface AddressRepo extends JpaRepository<Address, Long> {
        List<Address> findByUserIdOrderByIdDesc(Long userId);
    }
    public interface RestaurantRepo extends JpaRepository<Restaurant, Long> {}
    public interface MenuItemRepo extends JpaRepository<MenuItem, Long> {
        List<MenuItem> findByRestaurantId(Long restaurantId);
        List<MenuItem> findByNameContainingIgnoreCase(String q);
    }
    
    public interface OrderRepo extends JpaRepository<OrderEntity, Long> {
    
            @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderEntity o WHERE o.id = :id")
    Optional<OrderEntity> findByIdForUpdate(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = "items")
    List<OrderEntity> findAll();

    List<OrderEntity> findByUserIdOrderByIdDesc(Long userId);
    List<OrderEntity> findByRestaurantIdOrderByIdDesc(Long restaurantId);
    List<OrderEntity> findByStatusIn(Collection<OrderStatus> statuses);
    boolean existsByUserId(Long userId);
}
    public interface NotificationRepo
        extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByIdDesc(Long userId);
    long countByUserIdAndReadFalse(Long userId);

    List<Notification> findByRiderIdOrderByIdDesc(String riderId);
    Optional<Notification> findByIdAndRiderId(Long id, String riderId);
}
}
