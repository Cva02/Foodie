package com.foodie.customer.web;

import com.foodie.customer.dto.Dtos.NotificationDto;
import com.foodie.customer.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notifications;

    @GetMapping
    public List<NotificationDto> list(@RequestAttribute("uid") Long uid) {
        return notifications.list(uid);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unread(@RequestAttribute("uid") Long uid) {
        return Map.of("count", notifications.unread(uid));
    }

    @PostMapping("/{id}/read")
    public void read(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        notifications.markRead(uid, id);
    }

    @PostMapping("/read-all")
    public void readAll(@RequestAttribute("uid") Long uid) {
        notifications.markAllRead(uid);
    }
}
