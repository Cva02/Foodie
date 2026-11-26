package com.foodie.admin.rider.service;

import com.foodie.customer.model.Notification;
import com.foodie.customer.repo.Repos.NotificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiderNotificationService {

    private final NotificationRepo notificationRepo;

    public record RiderNotificationView(
            Long id,
            String type,
            String title,
            String message,
            Instant readAt,
            Instant createdAt
    ) {}

    @Transactional(readOnly = true)
    public List<RiderNotificationView> getNotifications(String riderId) {
        return notificationRepo.findByRiderIdOrderByIdDesc(riderId)
                .stream()
                .limit(100)
                .map(this::toView)
                .toList();
    }

    @Transactional
    public RiderNotificationView markAsRead(String riderId, Long notificationId) {
        Notification notification = notificationRepo
                .findByIdAndRiderId(notificationId, riderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found."
                ));

        notification.setRead(true);
        return toView(notification);
    }

    private RiderNotificationView toView(Notification notification) {
        return new RiderNotificationView(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead() ? Instant.now() : null,
                notification.getCreatedAt()
        );
    }
}