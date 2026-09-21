package com.billbox.alert;

import com.billbox.security.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertDtos> list() {
        return alertService.list(SecurityUtils.current());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", alertService.unreadCount(SecurityUtils.current()));
    }

    @PostMapping("/{id}/read")
    public void markRead(@PathVariable UUID id) {
        alertService.markRead(SecurityUtils.current(), id);
    }

    @PostMapping("/read-all")
    public void markAllRead() {
        alertService.markAllRead(SecurityUtils.current());
    }
}
