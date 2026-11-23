package com.foodie.customer.service;

import com.foodie.customer.dto.Dtos.NotificationDto;
import com.foodie.customer.model.Notification;
import com.foodie.customer.repo.Repos.NotificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepo repo;

    public void toUser(Long userId, String type, String title, String message, Long orderId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setOrderId(orderId);
        repo.save(n);
    }

    /** Seller-side notification (shown in the restaurant/admin portal). */
    public void toRestaurant(Long restaurantId, String type, String title, String message, Long orderId) {
        Notification n = new Notification();
        n.setRestaurantId(restaurantId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setOrderId(orderId);
        repo.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> list(Long userId) {
        return repo.findByUserIdOrderByIdDesc(userId).stream().limit(100)
                .map(n -> new NotificationDto(n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.isRead(),
                        n.getOrderId(), n.getCreatedAt())).toList();
    }

    public long unread(Long userId) {
        return repo.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long id) {
        repo.findById(id).filter(n -> userId.equals(n.getUserId())).ifPresent(n -> n.setRead(true));
    }

    @Transactional
    public void markAllRead(Long userId) {
        repo.findByUserIdOrderByIdDesc(userId).forEach(n -> n.setRead(true));
    }
}
